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
        name = "contrats",
        indexes = {
                @Index(name = "idx_contrat_employe_id", columnList = "employe_id"),
                @Index(name = "idx_contrat_statut", columnList = "statut"),
                @Index(name = "idx_contrat_type", columnList = "type")
        }
)
@Getter
@Setter
@Accessors(chain = true)
public class Contrat extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TypeContrat type;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    @Column(name = "salaire_base", nullable = false, precision = 15, scale = 2)
    private BigDecimal salaireBase;

    @Column(length = 100)
    private String poste;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutContrat statut = StatutContrat.ACTIF;

    public void valider() {
        if (dateDebut == null) {
            throw new BadRequestException("La date de début du contrat est obligatoire.");
        }
        if (dateFin != null && dateFin.isBefore(dateDebut)) {
            throw new BadRequestException(
                    "La date de fin ne peut pas être antérieure à la date de début.");
        }
        if (salaireBase == null || salaireBase.signum() <= 0) {
            throw new BadRequestException("Le salaire de base doit être positif.");
        }
        if (type == null) {
            throw new BadRequestException("Le type de contrat est obligatoire.");
        }
        if (type == TypeContrat.CDD && dateFin == null) {
            throw new BadRequestException("Un CDD doit avoir une date de fin.");
        }
    }

    /**
     * True if this contract overlaps [periodeDebut, periodeFin].
     * CDI: dateFin may be null (open-ended).
     */
    public boolean couvre(LocalDate periodeDebut, LocalDate periodeFin) {
        if (periodeDebut == null || periodeFin == null || dateDebut == null) {
            return false;
        }
        boolean startsBeforePeriodEnds = !dateDebut.isAfter(periodeFin);
        boolean endsAfterPeriodStarts =
                dateFin == null || !dateFin.isBefore(periodeDebut);
        return startsBeforePeriodEnds && endsAfterPeriodStarts;
    }

    public void resilier(LocalDate dateFinEffective) {
        if (statut != StatutContrat.ACTIF) {
            throw new BadRequestException("Seul un contrat actif peut être résilié.");
        }
        this.statut = StatutContrat.RESILIE;
        this.dateFin = dateFinEffective != null ? dateFinEffective : LocalDate.now();
    }

    public void terminer() {
        if (statut != StatutContrat.ACTIF) {
            throw new BadRequestException("Seul un contrat actif peut être terminé.");
        }
        this.statut = StatutContrat.TERMINE;
        if (this.dateFin == null) {
            this.dateFin = LocalDate.now();
        }
    }
}