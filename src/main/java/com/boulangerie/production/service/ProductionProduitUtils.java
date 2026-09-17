package com.boulangerie.production.service;

import com.boulangerie.production.model.TypePainProduction;

public final class ProductionProduitUtils {

    private ProductionProduitUtils() {
    }

    public static TypePainProduction determinerType(String libelle) {

        return switch (libelle) {

            case "GP_1KG" ->
                    TypePainProduction.GP;

            case "GP_1/2KG" ->
                    TypePainProduction.PP;

            default ->
                    null;
        };
    }
}