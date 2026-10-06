// ========== RECETTE ==========
package com.boulangerie.administration.dto;

public record RecetteFilter(
        Integer version,
        Boolean actif,
        String produitNom
) {}