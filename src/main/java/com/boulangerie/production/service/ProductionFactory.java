package com.boulangerie.production.service;

import com.boulangerie.administration.model.Recette;
import com.boulangerie.production.dto.ProductionPlan;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class ProductionFactory {
    public ProductionPlan creerPlan(Recette recette, BigDecimal sacsFarineUtilises) {
        BigDecimal multiplicateur = recette.calculerMultiplicateur(sacsFarineUtilises);
        return new ProductionPlan(
                recette.calculerQuantitePrevue(
                        multiplicateur
                ),
                recette.calculerConsommations(
                        multiplicateur
                )
        );
    }
}
