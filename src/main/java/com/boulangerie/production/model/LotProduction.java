// production/model/LotProduction.java
package com.boulangerie.production.model;

import com.boulangerie.production.dto.DestinationRequestDto;
import com.boulangerie.production.exception.DepassementRepartitionException;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "lots_production", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"date", "produit_id"})
})
@Getter
@Setter
@Accessors(chain = true)
public class LotProduction extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable =false)
    private Long produitId;

    @Column(nullable = false)
    private Long recetteId;

    /**
     * Number of flour bags requested by the baker.
     */
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal sacsFarineUtilises;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantitePrevue;

    @Column(precision = 10, scale = 2)
    private BigDecimal quantiteRealisee;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutProduction statut;

    @OneToMany(mappedBy = "lot", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<DestinationProduction> destinations = new ArrayList<>();

    public DestinationProduction ajouterDestination(
            DestinationRequestDto dto
    ) {
        verifierDestinationUnique(dto);
        DestinationProduction destinationProduction = DestinationProduction.creer(this, dto);
        this.destinations.add(destinationProduction);
        return destinationProduction;
    }

    public List<DestinationProduction> ajouterDestinations(
            List<DestinationRequestDto> demandes) {

        List<DestinationProduction> nouvelles = new ArrayList<>();

        for (DestinationRequestDto dto : demandes) {

            DestinationProduction destination = ajouterDestination(dto);

            nouvelles.add(destination);
        }

        return nouvelles;
    }

    private void verifierDestinationUnique(
            DestinationRequestDto dto
    ) {

        boolean existe = destinations.stream()
                .anyMatch(destination -> destination.estMemeDestination(dto));

        if (existe) {
            throw new BadRequestException("Cette destination existe déjà pour cette production.");
        }
    }
    public void finaliser(BigDecimal quantiteRealisee) {

        verifierNonFinalisee();
        verifierQuantiteRealisee(quantiteRealisee);

        this.quantiteRealisee = quantiteRealisee;
        this.statut = StatutProduction.TERMINEE;
    }

    public void verifierDistributionPossible() {
        if (statut != StatutProduction.TERMINEE) {
            throw new BadRequestException("La production doit être terminée.");
        }
    }

    public void verifierQuantiteDistribuable(BigDecimal dejaDistribue, BigDecimal demande) {
        BigDecimal disponible = quantiteRealisee.subtract(dejaDistribue);
        if (demande.compareTo(disponible) > 0) {
            throw new DepassementRepartitionException(id, disponible, demande);
        }
    }

    private void verifierNonFinalisee() {

        if (statut == StatutProduction.TERMINEE) {
            throw new BadRequestException("Production déjà finalisée.");
        }
    }

    private void verifierQuantiteRealisee(BigDecimal quantite) {
        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {

            throw new BadRequestException("La quantité réalisée doit être positive.");
        }

        if (quantite.compareTo(quantitePrevue) > 0) {
            throw new BadRequestException("La quantité réalisée dépasse la quantité prévue.");
        }
    }
}