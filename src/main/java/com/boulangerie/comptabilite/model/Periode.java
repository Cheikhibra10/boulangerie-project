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
@Table(
        name = "periodes",
        indexes = {
                @Index(name = "idx_periode_date_debut", columnList = "date_debut"),
                @Index(name = "idx_periode_statut", columnList = "statut"),
                @Index(name = "idx_periode_dates_statut", columnList = "date_debut,date_fin,statut")
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

    @Column(name = "date_fin", nullable =false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPeriode statut;

    @Column(name = "benefice_report", precision = 15, scale = 2, nullable = false)
    private BigDecimal beneficeReport;

    @OneToOne(mappedBy = "periode", cascade = CascadeType.ALL, orphanRemoval = true)
    private ResultatPeriode resultat;

    public static Periode creer(
            LocalDate debut,
            LocalDate fin) {

        verifierDates(debut, fin);

        Periode periode = new Periode();
        periode.dateDebut = debut;
        periode.dateFin = fin;
        periode.statut = StatutPeriode.OUVERTE;
        periode.beneficeReport = BigDecimal.ZERO;

        return periode;
    }

    public void fermer() {

        if (statut != StatutPeriode.OUVERTE) {
            throw new PeriodeDejaFermeeException(id);
        }

        statut = StatutPeriode.FERMEE;
    }

    public void rouvrir() {

        if (statut != StatutPeriode.FERMEE) {
            throw new IllegalStateException(
                    "Seule une période fermée peut être rouverte.");
        }

        statut = StatutPeriode.OUVERTE;
    }

    public void cloturer() {

        if (statut != StatutPeriode.FERMEE) {
            throw new IllegalStateException("Une période doit être fermée avant sa clôture.");
        }

        if (resultat == null) {throw new IllegalStateException("Impossible de clôturer une période sans résultat.");
        }

        statut = StatutPeriode.CLOTUREE;
    }

    public void enregistrerResultat(ResultatPeriode resultat) {

        if (this.resultat != null) {
            throw new ResultatDejaGenereException(id);
        }

        this.resultat = resultat;
    }

    public void initialiserBeneficeReport(BigDecimal montant) {

        if (montant == null || montant.signum() <= 0) {
            this.beneficeReport = BigDecimal.ZERO;
            return;
        }

        this.beneficeReport = montant;
    }


    public boolean estOuverte() {
        return statut == StatutPeriode.OUVERTE;
    }

    public boolean estFermee() {
        return statut == StatutPeriode.FERMEE;
    }

    public boolean estCloturee() {
        return statut == StatutPeriode.CLOTUREE;
    }

    public boolean contientResultat() {
        return resultat != null;
    }

    private static void verifierDates(
            LocalDate debut,
            LocalDate fin) {

        if (debut == null || fin == null) {
            throw new DateInvalideException("Dates obligatoires.");
        }

        if (!fin.equals(debut.plusMonths(1).minusDays(1))) {
            throw new DateInvalideException("Une période doit durer exactement un mois.");
        }
    }

    public void verifierCloturable() {

        if (!estFermee()) {
            throw new PeriodeNonCloturableException(id, statut);
        }

        if (resultat != null) {
            throw new ResultatDejaGenereException(id);
        }
    }
}