package com.boulangerie.comptabilite.model;

import com.boulangerie.comptabilite.utils.FinancialConstants;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "resultat_periode")
@Getter
@Setter(AccessLevel.PUBLIC)
public class ResultatPeriode extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn (name = "periode_id", unique = true, nullable = false)
    private Periode periode;

    @Column(name = "ca_abonnements", precision = 15, scale = 2, nullable = false)
    private BigDecimal caAbonnements = BigDecimal.ZERO;

    @Column(name = "ca_ventes_livreurs", precision = 15, scale = 2, nullable = false)
    private BigDecimal caVentesLivreurs = BigDecimal.ZERO;

    @Column(name = "ca_ventes_boutique", precision = 15, scale = 2, nullable = false)
    private BigDecimal caVentesBoutique = BigDecimal.ZERO;

    @Column(name = "ca_vente_restants", precision = 15, scale = 2, nullable = false)
    private BigDecimal caVenteRestants = BigDecimal.ZERO;

    @Column(name = "ca_autres_produits", precision = 15, scale = 2, nullable = false)
    private BigDecimal caAutresProduits = BigDecimal.ZERO;

    @Column(name = "total_charges", precision = 15, scale = 2, nullable = false)
    private BigDecimal totalCharges = BigDecimal.ZERO;

    @Column(name = "reliquat_livreurs_deduit", precision = 15, scale = 2, nullable = false)
    private BigDecimal reliquatLivreursDeduit = BigDecimal.ZERO;

    @Column(name = "credits_deduits", precision = 15, scale = 2, nullable = false)
    private BigDecimal creditsDeduits = BigDecimal.ZERO;

    @Column(name = "part_gerant", precision = 15, scale = 2, nullable = false)
    private BigDecimal partGerant = BigDecimal.ZERO;

    @Column(name = "part_boulangerie", precision = 15, scale = 2, nullable = false)
    private BigDecimal partBoulangerie = BigDecimal.ZERO;

    // ===== FACTORY METHOD =====
    public static ResultatPeriode creer(Periode periode, ResultatData data) {
        ResultatPeriode resultat = new ResultatPeriode();
        resultat.periode = periode;
        resultat.caAbonnements = data.getCaAbonnements();
        resultat.caVentesLivreurs = data.getCaVentesLivreurs();
        resultat.caVentesBoutique = data.getCaVentesBoutique();
        resultat.caVenteRestants = data.getCaVenteRestants();
        resultat.caAutresProduits = data.getCaAutresProduits();
        resultat.totalCharges = data.getTotalCharges();
        resultat.reliquatLivreursDeduit = data.getReliquatLivreurs();
        resultat.creditsDeduits = data.getCreditsClients();
        resultat.partGerant = data.getPartGerant();
        resultat.partBoulangerie = data.getPartBoulangerie();
        return resultat;
    }

    // ===== QUERY METHODS =====

    public BigDecimal getCaTotal() {
        return caAbonnements
                .add(caVentesLivreurs)
                .add(caVentesBoutique)
                .add(caVenteRestants)
                .add(caAutresProduits);
    }

    public BigDecimal getBeneficeBrut() {
        return getCaTotal().subtract(totalCharges);
    }

    public BigDecimal getBeneficeDistribuable() {
        return getBeneficeBrut()
                .subtract(reliquatLivreursDeduit)
                .subtract(creditsDeduits);
    }

    public BigDecimal getBeneficeNet() {
        return partGerant.add(partBoulangerie);
    }

    public boolean estBeneficiaire() {
        return getBeneficeNet().compareTo(FinancialConstants.MIN_BENEFIT_THRESHOLD) > 0;
    }
}