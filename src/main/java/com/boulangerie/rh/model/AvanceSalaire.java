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
        name = "avances_salaire",
        indexes = {
                @Index(name = "idx_avance_employe_id", columnList = "employe_id"),
                @Index(name = "idx_avance_statut", columnList = "statut"),
                @Index(name = "idx_avance_date", columnList = "date_demande")
        }
)
@Getter
@Setter
@Accessors(chain = true)
public class AvanceSalaire extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employe_id", nullable = false)
    private Employe employe;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal montant;

    @Column(name = "date_demande", nullable = false)
    private LocalDate dateDemande;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutAvance statut = StatutAvance.EN_ATTENTE;

    @Column(name = "bulletin_id")
    private Long bulletinId;

    public void valider() {
        if (montant == null || montant.signum() <= 0) {
            throw new BadRequestException("Le montant de l'avance doit être positif.");
        }
        if (dateDemande == null) {
            throw new BadRequestException("La date de demande est obligatoire.");
        }
    }

    public void approuver() {
        if (statut != StatutAvance.EN_ATTENTE) {
            throw new BadRequestException("Seule une avance en attente peut être approuvée.");
        }
        this.statut = StatutAvance.APPROUVEE;
    }

    public void refuser() {
        if (statut != StatutAvance.EN_ATTENTE) {
            throw new BadRequestException("Seule une avance en attente peut être refusée.");
        }
        this.statut = StatutAvance.REFUSEE;
    }

    public void marquerDeduite(Long bulletinId) {
        if (statut != StatutAvance.APPROUVEE) {
            throw new BadRequestException("Seule une avance approuvée peut être déduite.");
        }
        if (bulletinId == null) {
            throw new BadRequestException("Le bulletin de paie est obligatoire pour la déduction.");
        }
        this.statut = StatutAvance.DEDUITE;
        this.bulletinId = bulletinId;
    }

    public void annuler() {
        if (statut == StatutAvance.DEDUITE) {
            throw new BadRequestException("Une avance déjà déduite ne peut pas être annulée.");
        }
        this.statut = StatutAvance.ANNULEE;
    }
}