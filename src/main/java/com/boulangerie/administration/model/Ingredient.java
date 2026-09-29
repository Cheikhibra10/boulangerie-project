package com.boulangerie.administration.model;

import com.boulangerie.shared.model.AbstractAuditingEntity;
import com.boulangerie.shared.model.Activable;
import com.boulangerie.shared.model.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Table(name = "ingredients", indexes = {
        @Index(name = "idx_ingredients_actif", columnList = "actif")
})
@Getter
@Setter
@Accessors(chain = true)
public class Ingredient extends AbstractAuditingEntity implements GenericEntity<Ingredient>, Activable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "libelle", length = 100, nullable = false)
    private String libelle;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false,length = 20)
    private UniteMesure unite= UniteMesure.SAC;

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

    public boolean estLevure() {
        return libelle != null &&
                libelle.toLowerCase().contains("levure");
    }

    public boolean estAmeliorant() {
        return libelle != null &&
                libelle.toLowerCase().contains("ameliorant");
    }


    /**
     * Convertit le prix d'une unité achetée
     * vers le prix d'une unité de stock.
   */



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
        if (libelle.contains("ameliorant")) {
            return StockConstants.SEUIL_AMELIORANT;
        }
        return StockConstants.SEUIL_PAR_DEFAUT;
    }
}