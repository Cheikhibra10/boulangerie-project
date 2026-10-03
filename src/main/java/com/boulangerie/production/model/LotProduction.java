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
import java.util.*;

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

    public List<DestinationProduction> ajouterDestinations(List<DestinationRequestDto> demandes) {

        List<DestinationProduction> nouvelles = new ArrayList<>();

        for (DestinationRequestDto dto : demandes) {

            // 1. Create the original destination (ABONNEMENT, LIVREUR, BOUTIQUE...)
            DestinationProduction destination = ajouterDestination(dto);
            nouvelles.add(destination);

            // 2. Special rule for ABONNEMENT canal
            if (dto.getCanal() == CanalDistribution.ABONNEMENT) {

                if (dto.getLivreurId() == null) {
                    throw new BadRequestException(
                            "livreurId is required when distributing to an ABONNEMENT"
                    );
                }

                // --- Automatically credit the livreur with the same quantity ---
                // This represents the physical bread given to the livreur.
                // It does NOT affect the qteAbonnement check in Compte Rendu.

                Optional<DestinationProduction> existingLivreurDest = this.destinations.stream()
                        .filter(d -> d.getCanal() == CanalDistribution.LIVREUR
                                && Objects.equals(d.getLivreurId(), dto.getLivreurId()))
                        .findFirst();

                if (existingLivreurDest.isPresent()) {
                    // Increase existing LIVREUR destination
                    DestinationProduction existing = existingLivreurDest.get();
                    existing.setQuantite(existing.getQuantite().add(dto.getQuantite()));
                } else {
                    // Create a new LIVREUR destination
                    DestinationRequestDto livreurDto = new DestinationRequestDto();
                    livreurDto.setCanal(CanalDistribution.LIVREUR);
                    livreurDto.setLivreurId(dto.getLivreurId());
                    livreurDto.setQuantite(dto.getQuantite());
                    livreurDto.setPrixUnitaire(dto.getPrixUnitaire());
                    livreurDto.setEtatPain(dto.getEtatPain() != null ? dto.getEtatPain() : EtatPain.FRAIS);

                    DestinationProduction livreurDestination = ajouterDestination(livreurDto);
                    nouvelles.add(livreurDestination);
                }
            }
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

        verifierPeutEtreFinalisee(quantiteRealisee);

        this.quantiteRealisee = quantiteRealisee;
        this.statut = StatutProduction.TERMINEE;
    }

    public void verifierPeutEtreFinalisee(
            BigDecimal quantiteRealisee
    ) {
        if (statut != StatutProduction.PLANIFIEE) {
            throw new BadRequestException(
                    "Seule une production planifiée peut être finalisée."
            );
        }

        verifierQuantiteRealisee(quantiteRealisee);
    }

    public void verifierDistributionPossible() {
        if (statut != StatutProduction.TERMINEE) {
            throw new BadRequestException("La production doit être terminée.");
        }

    if(quantiteRealisee == null || quantiteRealisee.compareTo(BigDecimal.ZERO) <= 0) {
        throw new BadRequestException("La production terminée doit avoir une quantité réalisée.");
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

    }

    public BigDecimal getQuantiteDistribuee() {
        return destinations.stream()
                .map(DestinationProduction::getQuantite)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getQuantiteRestante() {
        BigDecimal realisee = quantiteRealisee == null ? BigDecimal.ZERO : quantiteRealisee;
        return realisee.subtract(getQuantiteDistribuee());
    }
}