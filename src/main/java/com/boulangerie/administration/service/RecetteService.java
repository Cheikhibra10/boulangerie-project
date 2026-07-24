// administration/service/RecetteService.java
package com.boulangerie.administration.service;

import com.boulangerie.administration.dto.RecetteDto;
import com.boulangerie.administration.model.Recette;
import com.boulangerie.shared.service.DefaultService;
import org.springframework.transaction.annotation.Transactional;

public interface RecetteService extends DefaultService<Recette, RecetteDto> {

    /**
     * Récupère la recette active d'un produit
     * Utilisé par : module production (US-01)
     */
    RecetteDto getActiveByProduitId(Long produitId);

    /**
     * Vérifie si un produit a une recette active
     */
    boolean hasActiveRecette(Long produitId);

    /**
     * Active ou désactive une recette (une seule recette active par produit)
     */
    RecetteDto setActive(Long recetteId, boolean actif);

    Recette findActiveByProduitIdOrThrow(Long produitId);

    @Transactional(readOnly = true)
    Recette findByIdOrThrow(Long recetteId);
}