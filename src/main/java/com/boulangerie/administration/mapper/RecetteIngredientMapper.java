// administration/mapper/RecetteIngredientMapper.java
package com.boulangerie.administration.mapper;

import com.boulangerie.administration.dto.RecetteIngredientDto;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.model.RecetteIngredient;
import com.boulangerie.administration.repository.IngredientRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class RecetteIngredientMapper implements EntityMapper<RecetteIngredientDto, RecetteIngredient> {

    @Override
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "recette", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract RecetteIngredient toEntity(RecetteIngredientDto dto);

    @Override
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientLibelle", source = "ingredient.libelle")
    public abstract RecetteIngredientDto toDto(RecetteIngredient entity);

}