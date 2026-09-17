// production/model/DestinationProduction.java
package com.boulangerie.production.model;

import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.administration.model.Livreur;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.production.dto.DestinationRequestDto;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

@Getter
@Setter
@Entity
@Table(name = "destinations_production")
@Accessors(chain = true)
public class DestinationProduction extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lot_id", nullable = false)
    private LotProduction lot;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CanalDistribution canal;
    @Enumerated(EnumType.STRING)
    @Column(name = "etat_pain", nullable = false)
    private EtatPain etatPain = EtatPain.FRAIS;
    @Column(nullable = false)
    private Long produitId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal quantite;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prixUnitaire;
    @Column(name = "quantite_consommee", precision = 10, scale = 2)
    private BigDecimal quantiteConsommee = BigDecimal.ZERO;
    @Column
    private Long livreurId;
    @Column
    private Long abonnementId;

    public static DestinationProduction creer(
            LotProduction lot,
            DestinationRequestDto dto) {

        DestinationProduction destination = new DestinationProduction();

        destination.setLot(lot);
        destination.setDate(lot.getDate());
        destination.setProduitId(lot.getProduitId());

        destination.setCanal(dto.getCanal());
        destination.setEtatPain(dto.getEtatPain());
        destination.setQuantite(dto.getQuantite());
        destination.setPrixUnitaire(dto.getPrixUnitaire());
        destination.setLivreurId(dto.getLivreurId());
        destination.setAbonnementId(dto.getAbonnementId());

        if(dto.getCanal() == CanalDistribution.ABONNEMENT){
            destination.setLivreurId(null);
        } else if (dto.getCanal() == CanalDistribution.LIVREUR) {
            destination.setAbonnementId(null);
        }
        destination.valider();

        return destination;
    }

    private void verifierBoutique() {
        if (canal != CanalDistribution.BOUTIQUE) {
            throw new BadRequestException(
                    "Cette opération est réservée aux destinations BOUTIQUE.");
        }
    }

    public BigDecimal getQuantiteDisponible() {
        verifierBoutique();
        return quantite.subtract(quantiteConsommee);
    }

    public void enregistrerVente(BigDecimal qteVendue) {
        verifierBoutique();
        if (qteVendue == null || qteVendue.signum() <= 0) {
            throw new BadRequestException("Quantité invalide.");
        }
        BigDecimal disponible = getQuantiteDisponible();
        if (qteVendue.compareTo(disponible) > 0) {
            throw new BadRequestException("Stock boutique insuffisant.");
        }
        quantiteConsommee = quantiteConsommee.add(qteVendue);
    }

    public void valider() {
        verifierQuantite();
        verifierLivreur();
        verifierAbonnement();
        verifierUniciteDestination();
    }

    private void verifierQuantite() {

        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La quantité distribuée doit être positive.");
        }
    }

    private void verifierLivreur() {

        if (canal == CanalDistribution.LIVREUR && livreurId == null) {

            throw new BadRequestException("Le livreur est obligatoire.");
        }



        if (canal != CanalDistribution.LIVREUR
                && livreurId != null) {

            throw new BadRequestException(
                    "Livreur invalide pour ce canal."
            );
        }
    }


    private void verifierUniciteDestination() {

        if (livreurId != null && abonnementId != null) {
            throw new BadRequestException(
                    "Une destination ne peut pas être à la fois un livreur et un abonnement."
            );
        }
    }
    private void verifierAbonnement() {

        if (canal == CanalDistribution.ABONNEMENT
                && abonnementId == null) {

            throw new BadRequestException(
                    "L'abonnement est obligatoire."
            );
        }

        if (canal != CanalDistribution.ABONNEMENT
                && abonnementId != null) {

            throw new BadRequestException(
                    "Abonnement invalide pour ce canal."
            );
        }
    }

    public boolean estMemeDestination(DestinationRequestDto dto) {
        if (canal != dto.getCanal()) {
            return false;
        }

        return switch (canal) {

            case LIVREUR -> Objects.equals(livreurId, dto.getLivreurId());

            case ABONNEMENT -> Objects.equals(abonnementId, dto.getAbonnementId());
            case AUMONE, VENTE_FAST_FOOD, RATION_PERSONNELLE, BOUTIQUE, AUTRES -> true;
        };
    }

    public void retourner(BigDecimal quantite) {
        verifierBoutique();
        if (quantite == null || quantite.signum() <= 0) {
            throw new BadRequestException("Quantité invalide.");
        }

        if (quantite.compareTo(quantiteConsommee) > 0) {
            throw new BadRequestException(
                    "Retour supérieur à la quantité vendue.");
        }
        quantiteConsommee = quantiteConsommee.subtract(quantite);
    }
}