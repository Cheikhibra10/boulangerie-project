// ventes/model/LigneVenteBoutique.java
package com.boulangerie.ventes.model;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Table(name = "lignes_vente_boutique")
@Getter
@Setter
@Accessors(chain = true)
public class LigneVenteBoutique extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vente_id", nullable = false)
    private VenteBoutique vente;

    @Column(nullable = false)
    private Long produitId;
    @Column(name = "produit_libelle", nullable = false, length = 150)
    private String produitLibelle;

    @Column(name = "quantite", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantite;

    @Column(name = "prix_unitaire", precision = 15, scale = 2, nullable = false)
    private BigDecimal prixUnitaire;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_vente", nullable = false)
    private TypeVenteLigne typeVente = TypeVenteLigne.NORMALE;
    @Column(name = "quantite_retournee", precision = 10, scale = 2, nullable = false)
    private BigDecimal quantiteRetournee = BigDecimal.ZERO;

    public void initialiser(Long produitId, String produitLibelle, BigDecimal quantite, BigDecimal prixUnitaire){
        if (quantite == null || quantite.signum() <= 0) {
            throw new BadRequestException("La quantité doit être positive.");
        }

        if (prixUnitaire == null || prixUnitaire.signum() <= 0) {
            throw new BadRequestException("Le prix unitaire est invalide.");
        }
        this.produitId = produitId;
        this.produitLibelle = produitLibelle;
        this.quantite = quantite;
        this.prixUnitaire = prixUnitaire;
    }

    public BigDecimal getTotal(){
        return prixUnitaire.multiply(quantite);
    }

    public BigDecimal getQuantiteDisponibleRetour() {
        return quantite.subtract(quantiteRetournee);
    }

    public BigDecimal getMontantRetourne() {
        return quantiteRetournee.multiply(prixUnitaire);
    }

    public BigDecimal getMontantDisponibleRetour() {
        return getQuantiteDisponibleRetour().multiply(prixUnitaire);
    }

    public void retourner(BigDecimal quantiteRetour) {

        if (quantiteRetour == null || quantiteRetour.signum() <= 0) {
            throw new BadRequestException("La quantité retournée doit être positive.");
        }

        if (quantiteRetour.compareTo(getQuantiteDisponibleRetour()) > 0) {
            throw new BadRequestException(
                    "Impossible de retourner " + quantiteRetour +
                            ". Quantité restante : " + getQuantiteDisponibleRetour());
        }

        this.quantiteRetournee = this.quantiteRetournee.add(quantiteRetour);
    }
}