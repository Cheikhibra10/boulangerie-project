// livreurs/model/LigneCompteLivreur.java
package com.boulangerie.livreurs.model;

import com.boulangerie.production.model.DestinationProduction;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_compte_livreur")
@Getter
@Setter
@Accessors(chain = true)
public class LigneCompteLivreur extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journalier_id", nullable = false)
    private CompteLivreurJournalier journalier;

    @Column(name = "destination_id", nullable = false)
    private Long destinationId;

    @Column(name = "qte_livree", precision = 10, scale = 2, nullable = false)
    private BigDecimal qteLivree;
    @Column(name = "qte_abonnement", precision = 10, scale = 2, nullable = false)
    private BigDecimal qteAbonnement = BigDecimal.ZERO;
    @Column(name = "retours", precision = 10, scale = 2, nullable = false)
    private BigDecimal retours = BigDecimal.ZERO;
    @Column(name = "qte_offerte", precision = 10, scale = 2, nullable = false)
    private BigDecimal qteOfferte = BigDecimal.ZERO;
    @Column(name = "qte_nette", precision = 10, scale = 2, nullable = false)
    private BigDecimal qteNette;
    @Column(name = "prix_unitaire", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixUnitaire;
    @Column(name = "montant_vente", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantVente;
    @Column(name = "prix_commission", precision = 10, scale = 2, nullable = false)
    private BigDecimal prixCommission;
    @Column(name = "montant_commission", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantCommission;
    @Column(name = "depenses_livreur", precision = 15, scale = 2, nullable = false)
    private BigDecimal depensesLivreur = BigDecimal.ZERO;
    @Column(name = "montant_apres_deduction", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantApresDeduction;

    public void initialiser(BigDecimal prixUnitaire, BigDecimal prixCommission, Long destinationId) {
        this.destinationId = destinationId;
        this.prixUnitaire = prixUnitaire;
        this.prixCommission = prixCommission;
        calculerMontants();
    }

    public void calculerMontants() {

        qteNette = qteLivree
                .subtract(qteAbonnement)
                .subtract(retours)
                .subtract(qteOfferte);

        montantVente = qteNette.multiply(prixUnitaire);
        montantCommission = qteNette.multiply(prixCommission);
        montantApresDeduction = montantCommission.subtract(depensesLivreur);
    }
}