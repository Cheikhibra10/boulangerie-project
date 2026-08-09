// ventes/model/VenteBoutique.java
package com.boulangerie.ventes.model;

import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "ventes_boutique", indexes = {
        @Index(name = "idx_vente_caisse", columnList = "caisse_id"),
        @Index(name = "idx_vente_date", columnList = "date")
})
@Getter
@Setter
@Accessors(chain = true)
public class VenteBoutique extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable=false)
    private LocalDate date;

    @Column(nullable = false)
    private Long caisseId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "utilisateur_id", nullable = false)
    private Utilisateur utilisateur;

    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private StatutVente statut = StatutVente.OUVERTE;

    @OneToMany(mappedBy="vente", cascade=CascadeType.ALL, orphanRemoval=true)
    private List<LigneVenteBoutique> lignes = new ArrayList<>();

    @OneToOne(mappedBy="vente", cascade=CascadeType.ALL)
    private Paiement paiement;

    @Column(nullable=false, precision=15, scale=2)
    private BigDecimal total = BigDecimal.ZERO;

    @Column(length = 255)
    private String motif_annulation;

    public void ajouterLigne(LigneVenteBoutique ligne){
        ligne.setVente(this);
        lignes.add(ligne);
    }

    public void supprimerLigne(Long ligneId){
        lignes.removeIf(l -> l.getId().equals(ligneId));
        finaliser();
    }

    private void recalculerTotal() {
        total = lignes.stream()
                .map(LigneVenteBoutique::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void finaliser() {
        verifierModifiable();
        recalculerTotal();
    }

    public void annuler(String motif) {

        verifierAnnulationPossible();

        if (motif == null || motif.isBlank()) {
            throw new BadRequestException("Le motif d'annulation est obligatoire.");
        }

        this.statut = StatutVente.ANNULEE;
        this.motif_annulation = motif;
    }

    private void verifierAnnulationPossible() {

        if (statut != StatutVente.PAYEE) {
            throw new BadRequestException(
                    "Seule une vente payée peut être annulée.");
        }
    }

    public void encaisser(Paiement paiement) {
        verifierModifiable();
        if (paiement.getMontant().compareTo(total) != 0) {
            throw new BadRequestException("Le paiement doit être exactement " + total);
        }
        this.paiement = paiement;
        paiement.setVente(this);
        this.statut = StatutVente.PAYEE;
    }

    private void verifierModifiable() {
        if (statut != StatutVente.OUVERTE) {
            throw new BadRequestException("La vente est déjà payée.");
        }
    }

    public LigneVenteBoutique getLigne(Long produitId) {

        return lignes.stream()
                .filter(l -> l.getProduitId().equals(produitId))
                .findFirst()
                .orElseThrow(() ->
                        new EntityNotFoundException("Produit introuvable dans cette vente."));
    }

    public BigDecimal getMontantNet() {
        return total.subtract(getMontantRetourne());
    }

    public BigDecimal getMontantRetourne() {
        return lignes.stream()
                .map(LigneVenteBoutique::getMontantRetourne)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void verifierRetourPossible() {

        if(statut != StatutVente.PAYEE) {
            throw new BadRequestException("Seule une vente payée peut être retournée.");
        }

        Instant limiteRetour = getCreatedAt().plus(Duration.ofMinutes(10));
        if (Instant.now().isAfter(limiteRetour)) {
            throw new BadRequestException("Le délai maximal de retour (10 minutes) est dépassé.");
        }
    }

    public String getNumero() {
        return "VENTE-0000" + getId();
    }
}