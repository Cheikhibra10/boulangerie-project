package com.boulangerie.rh.model;

import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Entity
@Table(name = "absences", indexes = {
        @Index(name = "idx_absence_employe_id", columnList = "employe_id"),
        @Index(name = "idx_absence_statut", columnList = "statut"),
        @Index(name = "idx_absence_type", columnList = "type"),
        @Index(name = "idx_absence_dates", columnList = "date_debut, date_fin")
})
@Getter
@Setter
@Accessors(chain = true)
public class Absence extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TypeAbsence type;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin", nullable = false)
    private LocalDate dateFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutAbsence statut = StatutAbsence.EN_ATTENTE;

    @Column(length = 500)
    private String motif;

    public void valider() {
        if (dateDebut == null || dateFin == null) {
            throw new BadRequestException("Les dates de début et de fin sont obligatoires.");
        }
        if (dateFin.isBefore(dateDebut)) {
            throw new BadRequestException("La date de fin ne peut pas être antérieure à la date de début.");
        }
        if (type == null) {
            throw new BadRequestException("Le type d'absence est obligatoire.");
        }
    }

    public void approuver() {
        if (statut != StatutAbsence.EN_ATTENTE) {
            throw new BadRequestException("Seule une absence en attente peut être approuvée.");
        }
        this.statut = StatutAbsence.APPROUVEE;
    }

    public void refuser(String motifRefus) {
        if (statut != StatutAbsence.EN_ATTENTE) {
            throw new BadRequestException("Seule une absence en attente peut être refusée.");
        }
        this.statut = StatutAbsence.REFUSEE;
        if (motifRefus != null && !motifRefus.isBlank()) {
            this.motif = motifRefus;
        }
    }

    public void annuler() {
        if (statut == StatutAbsence.ANNULEE) {
            throw new BadRequestException("Cette absence est déjà annulée.");
        }
        this.statut = StatutAbsence.ANNULEE;
    }
}