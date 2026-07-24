// administration/mapper/IngredientMapper.java
package com.boulangerie.administration.mapper;

import com.boulangerie.administration.dto.IngredientDto;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface IngredientMapper extends EntityMapper<IngredientDto, Ingredient> {
}