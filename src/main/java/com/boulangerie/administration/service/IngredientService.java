// administration/service/IngredientService.java
package com.boulangerie.administration.service;

import com.boulangerie.administration.dto.IngredientDto;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.service.DefaultService;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IngredientService extends DefaultService<Ingredient, IngredientDto> {
    // Méthodes spécifiques si nécessaire
    Ingredient findIngredientOrThrow(Long id);
    Map<Long, Ingredient> findIngredientsByIds(List<Long> ingredientIds);

}