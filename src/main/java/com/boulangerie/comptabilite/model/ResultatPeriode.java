package com.boulangerie.comptabilite.model;

import com.boulangerie.comptabilite.utils.FinancialConstants;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(
        name = "resultat_periode",
        indexes = {
                @Index(name = "idx_resultat_periode", columnList = "periode_id")
        }
)
@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ResultatPeriode extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periode_id", nullable = false, unique = true)
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

    public static ResultatPeriode creer(Periode periode, ResultatData data) {

        Objects.requireNonNull(periode, "La période est obligatoire");
        Objects.requireNonNull(data, "Les données du résultat sont obligatoires");

        ResultatPeriode resultat = new ResultatPeriode();

        resultat.periode = periode;
        resultat.caAbonnements = normaliser(data.getCaAbonnements());
        resultat.caVentesLivreurs = normaliser(data.getCaVentesLivreurs());
        resultat.caVentesBoutique = normaliser(data.getCaVentesBoutique());
        resultat.caVenteRestants = normaliser(data.getCaVenteRestants());
        resultat.caAutresProduits = normaliser(data.getCaAutresProduits());
        resultat.totalCharges = normaliser(data.getTotalCharges());
        resultat.reliquatLivreursDeduit = normaliser(data.getReliquatLivreurs());
        resultat.creditsDeduits = normaliser(data.getCreditsClients());
        resultat.partGerant = normaliser(data.getPartGerant());
        resultat.partBoulangerie = normaliser(data.getPartBoulangerie());

        resultat.verifierMontants();

        return resultat;
    }

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
        return getBeneficeNet()
                .compareTo(FinancialConstants.MIN_BENEFIT_THRESHOLD) > 0;
    }

    public BigDecimal getMontantAReporter() {
        return partBoulangerie;
    }

    public boolean estDeficitaire() {
        return getBeneficeNet().signum() < 0;
    }

    private void verifierMontants() {

        verifier(caAbonnements);
        verifier(caVentesLivreurs);
        verifier(caVentesBoutique);
        verifier(caVenteRestants);
        verifier(caAutresProduits);
        verifier(totalCharges);
        verifier(reliquatLivreursDeduit);
        verifier(creditsDeduits);
        verifier(partGerant);
        verifier(partBoulangerie);
    }

    private static void verifier(BigDecimal montant) {
        if (montant == null || montant.signum() < 0) {
            throw new BadRequestException("Tous les montants doivent être positifs ou nuls.");
        }
    }

    private static BigDecimal normaliser(BigDecimal montant) {

        if (montant == null) {
            return BigDecimal.ZERO.setScale(
                    FinancialConstants.FINANCIAL_SCALE,
                    FinancialConstants.FINANCIAL_ROUNDING
            );
        }

        return montant.setScale(
                FinancialConstants.FINANCIAL_SCALE,
                FinancialConstants.FINANCIAL_ROUNDING
        );
    }
}