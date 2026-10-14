package com.boulangerie.rh.model;

import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(
        name = "presences",
        indexes = {
                @Index(name = "idx_presence_employe_id", columnList = "employe_id"),
                @Index(name = "idx_presence_date", columnList = "date"),
                @Index(name = "idx_presence_statut", columnList = "statut")
        },
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_presence_employe_date",
                        columnNames = {"employe_id", "date"}
                )
        }
)
@Getter
@Setter
@Accessors(chain = true)
public class Presence extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(nullable = false)
    private LocalDate date;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutPresence statut = StatutPresence.PRESENT;

    @Column(name = "heure_arrivee")
    private LocalTime heureArrivee;

    @Column(name = "heure_depart")
    private LocalTime heureDepart;

    public void valider() {
        if (date == null) {
            throw new BadRequestException("La date de présence est obligatoire.");
        }
        if (statut == null) {
            throw new BadRequestException("Le statut de présence est obligatoire.");
        }
        if (heureArrivee != null && heureDepart != null && heureDepart.isBefore(heureArrivee)) {
            throw new BadRequestException(
                    "L'heure de départ ne peut pas être antérieure à l'heure d'arrivée.");
        }
    }
}