// ========== CLIENT ==========
package com.boulangerie.abonnements.dto;

public record ClientFilter(
        String nom,
        String prenom,
        String telephone,
        Boolean actif
) {}