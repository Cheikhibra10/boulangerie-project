// livreurs/model/CompteLivreurJournalier.java
package com.boulangerie.livreurs.model;

import com.boulangerie.livreurs.exception.CompteRenduDejaClotureException;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Entity
@Table(name = "comptes_livreur_journalier", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"livreur_id", "date"})
})
@Getter
@Setter
@Accessors(chain = true)
public class CompteLivreurJournalier extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long livreurId;
    @Column(name = "date", nullable = false)
    private LocalDate date;
    @Column(name = "reliquat_report", precision = 15, scale = 2, nullable = false)
    private BigDecimal reliquatReport = BigDecimal.ZERO;
    @Column(name = "total_a_verser", precision = 15, scale = 2)
    private BigDecimal totalAVerser;
    @Column(name = "reliquat_fin", precision = 15, scale = 2)
    private BigDecimal reliquatFin;
    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutCompteRendu statut = StatutCompteRendu.BROUILLON;
    @OneToMany(mappedBy = "journalier", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<LigneCompteLivreur> lignes = new ArrayList<>();
    @OneToOne(mappedBy = "compteRendu", cascade = CascadeType.ALL, orphanRemoval = true)
    private VersementLivreur versement;

    public void addLigne(LigneCompteLivreur ligne) {
     verifierNonCloture();
     ligne.setJournalier(this);
     lignes.add(ligne);
     recalculerMontants();
    }

    public void verifierDestination(Long allocationLivreurId) {
        if (!livreurId.equals(allocationLivreurId)) {
            throw new BadRequestException("Cette destination n'appartient pas au livreur.");
        }
    }

    public BigDecimal calculerCommissionTotale() {
        return lignes.stream()
                .map(LigneCompteLivreur::getMontantApresDeduction)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal calculerMontantVente() {

        return lignes.stream()
                .map(LigneCompteLivreur::getMontantVente)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void recalculerMontants() {

        totalAVerser = Optional.ofNullable(reliquatReport)
                .orElse(BigDecimal.ZERO)
                .add(calculerMontantVente());

        if (versement != null) {
            reliquatFin = totalAVerser.subtract(versement.getMontant());
        }
    }

    public void cloturer(VersementLivreur versement) {
        verifierNonCloture();
        recalculerMontants();
        this.versement = versement;
        versement.setCompteRendu(this);
        reliquatFin = totalAVerser.subtract(versement.getMontant());
        statut = StatutCompteRendu.CLOTURE;
    }

    public void verifierNonCloture() {
        if (statut == StatutCompteRendu.CLOTURE) {
            throw new CompteRenduDejaClotureException(id);
        }
    }

    public BigDecimal getVersementsDuJour(){
        return versement == null ? BigDecimal.ZERO : versement.getMontant();
    }
}