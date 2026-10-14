package com.boulangerie.rh.model;

import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(
        name = "bulletins_paie",
        indexes = {
                @Index(name = "idx_bulletin_employe_id", columnList = "employe_id"),
                @Index(name = "idx_bulletin_periode_id", columnList = "periode_id"),
                @Index(name = "idx_bulletin_statut", columnList = "statut")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_bulletin_employe_periode",
                        columnNames = {"employe_id", "periode_id"}
                )
        }
)
@Getter
@Setter
@Accessors(chain = true)
public class BulletinPaie extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "contrat_id", nullable = false)
    private Contrat contrat;

    /** comptabilite.Periode — ID only */
    @Column(name = "periode_id", nullable = false)
    private Long periodeId;

    @Column(name = "salaire_brut", nullable = false, precision = 15, scale = 2)
    private BigDecimal salaireBrut;

    @Column(name = "retenues", nullable = false, precision = 15, scale = 2)
    private BigDecimal retenues = BigDecimal.ZERO;

    @Column(name = "net_a_payer", nullable = false, precision = 15, scale = 2)
    private BigDecimal netAPayer;

    /** Snapshot at generation time (from Absence) */
    @Column(name = "jours_travailles", precision = 5, scale = 1)
    private BigDecimal joursTravailles;

    @Column(name = "jours_absents", precision = 5, scale = 1)
    private BigDecimal joursAbsents = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutBulletin statut = StatutBulletin.BROUILLON;

    @Column(name = "date_paiement")
    private LocalDate datePaiement;

    public void recalculerNet() {
        BigDecimal brut = salaireBrut != null ? salaireBrut : BigDecimal.ZERO;
        BigDecimal ret = retenues != null ? retenues : BigDecimal.ZERO;
        this.netAPayer = brut.subtract(ret);
        if (netAPayer.signum() < 0) {
            throw new BadRequestException("Le net à payer ne peut pas être négatif.");
        }
    }

    public void valider() {
        if (statut != StatutBulletin.BROUILLON) {
            throw new BadRequestException("Seul un bulletin en brouillon peut être validé.");
        }
        recalculerNet();
        this.statut = StatutBulletin.VALIDE;
    }

    public void marquerPaye(LocalDate date) {
        if (statut != StatutBulletin.VALIDE) {
            throw new BadRequestException("Seul un bulletin validé peut être marqué comme payé.");
        }
        this.statut = StatutBulletin.PAYE;
        this.datePaiement = date != null ? date : LocalDate.now();
    }

    public void annuler() {
        if (statut == StatutBulletin.PAYE) {
            throw new BadRequestException("Un bulletin déjà payé ne peut pas être annulé.");
        }
        this.statut = StatutBulletin.ANNULE;
    }
}