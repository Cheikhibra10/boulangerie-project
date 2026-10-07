// ========== RECETTE ==========
package com.boulangerie.administration.dto;

public record RecetteFilter(
        Boolean actif,
        String produitLibelle
) {}