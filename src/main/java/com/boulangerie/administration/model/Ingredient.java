// administration/model/Ingredient.java
package com.boulangerie.administration.model;

import com.boulangerie.shared.dto.ValeursStock;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import com.boulangerie.shared.model.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Entity
@Table(name = "ingredients")
@Getter
@Setter
@Accessors(chain = true)
public class Ingredient extends AbstractAuditingEntity implements GenericEntity<Ingredient> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "libelle", length = 100, nullable = false)
    private String libelle;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private UniteMesure unite= UniteMesure.SAC;
    @Column(name = "equivalence_stock", precision = 10, scale = 2, nullable = false)
    private BigDecimal equivalenceStock = BigDecimal.ONE;
    @Column(name = "actif", nullable = false)
    private Boolean actif = true;
    @Override
    public Ingredient createNewInstance() {
        return new Ingredient();
    }

    public boolean estFarine() {
        return libelle != null &&
                libelle.toLowerCase().contains("farine");
    }

    public BigDecimal convertirQuantiteVersStock(BigDecimal quantiteSaisie) {
        if (quantiteSaisie == null) {
            throw new BadRequestException("Quantité obligatoire.");
        }

        if (quantiteSaisie.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La quantité doit être positive.");
        }
        return quantiteSaisie.multiply(equivalenceStock);
    }

    /**
     * Convertit le prix d'une unité achetée
     * vers le prix d'une unité de stock.
     */
    public BigDecimal convertirPrixVersStock(BigDecimal prixUnitaire) {

        if (prixUnitaire == null || prixUnitaire.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("Prix unitaire invalide.");
        }
        return prixUnitaire.divide(equivalenceStock, 6, RoundingMode.HALF_UP
        );
    }

    public ValeursStock convertirVersStock(
            BigDecimal quantite,
            BigDecimal prixUnitaire
    ) {
        return new ValeursStock(
                convertirQuantiteVersStock(quantite),
                convertirPrixVersStock(prixUnitaire)
        );
    }



    public BigDecimal getSeuilAlerteParDefaut() {
        String libelle = this.libelle.toLowerCase();
        if (libelle.contains("farine")) {
            return StockConstants.SEUIL_FARINE;
        }
        if (libelle.contains("sucre")) {
            return StockConstants.SEUIL_SUCRE;
        }
        if (libelle.contains("levure")) {
            return StockConstants.SEUIL_LEVURE;
        }
        return StockConstants.SEUIL_PAR_DEFAUT;
    }
}