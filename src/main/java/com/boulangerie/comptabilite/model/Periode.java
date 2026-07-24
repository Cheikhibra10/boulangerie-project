// comptabilite/model/Periode.java
package com.boulangerie.comptabilite.model;

import com.boulangerie.administration.model.StatutPeriode;
import com.boulangerie.comptabilite.exception.*;
import com.boulangerie.comptabilite.utils.FinancialConstants;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


@Entity
@Table(name = "periodes",
        indexes = {
                @Index(name = "idx_periode_date_debut", columnList = "dateDebut"),
                @Index(name = "idx_periode_statut", columnList = "statut"),
                @Index(name = "idx_periode_dates_statut", columnList = "dateDebut, dateFin, statut")
        }
)
@Getter
@Setter(AccessLevel.PRIVATE)
@Accessors(chain = true)
public class Periode extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_debut", nullable = false, unique = true)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutPeriode statut = StatutPeriode.OUVERTE;

    @Column(name = "benefice_report", precision = 15, scale = 2)
    private BigDecimal beneficeReport = BigDecimal.ZERO;

    @OneToOne(mappedBy = "periode", cascade = CascadeType.ALL, orphanRemoval = true)
    private ResultatPeriode resultat;

    // ===== FACTORY METHOD =====
    public static Periode nouvellePeriode(LocalDate dateDebut, LocalDate dateFin) {
        validerDates(dateDebut, dateFin);
        validerDureeMensuelle(dateDebut, dateFin);

        Periode periode = new Periode();
        periode.dateDebut = dateDebut;
        periode.dateFin = dateFin;
        periode.statut = StatutPeriode.OUVERTE;
        periode.beneficeReport = FinancialConstants.ZERO;
        return periode;
    }

    // ===== BUSINESS METHODS =====

    public void ouvrir() {
        if (this.statut == StatutPeriode.OUVERTE) {
            throw new PeriodeDejaOuverteException(this.id);
        }
        if (this.statut == StatutPeriode.CLOTUREE) {
            throw new PeriodeDejaClotureeException(this.id);
        }
        this.statut = StatutPeriode.OUVERTE;
    }

    public void fermer() {
        if (this.statut == StatutPeriode.FERMEE) {
            throw new PeriodeDejaFermeeException(this.id);
        }
        if (this.statut == StatutPeriode.CLOTUREE) {
            throw new PeriodeDejaClotureeException(this.id);
        }
        this.statut = StatutPeriode.FERMEE;
    }

    public void cloturer() {
        if (this.statut == StatutPeriode.CLOTUREE) {
            throw new PeriodeDejaClotureeException(this.id);
        }
        if (this.statut == StatutPeriode.FERMEE) {
            throw new PeriodeDejaFermeeException(this.id);
        }
        this.statut = StatutPeriode.CLOTUREE;
    }

    public void reporterBenefice(BigDecimal montant) {
        if (montant == null || montant.compareTo(FinancialConstants.MIN_BENEFIT_THRESHOLD) < 0) {
            return;
        }
        this.beneficeReport = this.beneficeReport.add(montant)
                .setScale(FinancialConstants.FINANCIAL_SCALE, FinancialConstants.FINANCIAL_ROUNDING);
    }



    public void attacherResultat(ResultatPeriode resultat) {
        if (this.statut != StatutPeriode.OUVERTE && this.statut != StatutPeriode.FERMEE) {
            throw new PeriodeNonModifiableException(this.id, this.statut);
        }
        if (this.resultat != null) {
            throw new ResultatDejaGenereException(this.id);
        }
        this.resultat = resultat;
        resultat.setPeriode(this);
    }

    public boolean hasResultat() {
        return this.resultat != null;
    }

    // ===== QUERY METHODS =====

    public boolean estOuverte() {
        return this.statut == StatutPeriode.OUVERTE;
    }


    // ===== PRIVATE HELPERS =====

    private static void validerDates(LocalDate debut, LocalDate fin) {
        if (fin.isBefore(debut)) {
            throw new DateInvalideException("La date de fin doit être après la date de début");
        }
        if (debut.isBefore(LocalDate.now())) {
            throw new DateInvalideException("La date de début ne peut pas être dans le passé");
        }
    }

    private static void validerDureeMensuelle(LocalDate debut, LocalDate fin) {
        // Validate it's approximately one month
        long totalJours = ChronoUnit.DAYS.between(debut, fin) + 1;

        // A month is between 28 and 31 days
        if (totalJours < 28 || totalJours > 31) {
            throw new DateInvalideException("La période doit être mensuelle (28-31 jours)" );
        }

        // Validate it's exactly one month (same day of month)
        LocalDate debutPlusMonth = debut.plusMonths(1).minusDays(1);
        if (!fin.equals(debutPlusMonth)) {
            throw new DateInvalideException("La période doit être d'un mois exact.");
        }
    }
}