// ========== ABONNEMENTS ==========
package com.boulangerie.abonnements.dto;

public record AbonnementFilter(
        Boolean actif,
        String livreurNom
) {}