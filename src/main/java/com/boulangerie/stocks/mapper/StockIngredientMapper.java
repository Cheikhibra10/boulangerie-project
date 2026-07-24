// stocks/mapper/StockIngredientMapper.java
package com.boulangerie.stocks.mapper;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.repository.IngredientRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.mapper.EntityMapper;
import com.boulangerie.stocks.dto.StockIngredientDto;
import com.boulangerie.stocks.model.StockIngredient;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class StockIngredientMapper implements EntityMapper<StockIngredientDto, StockIngredient> {


    // ===== TO ENTITY =====
    @Override
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract StockIngredient toEntity(StockIngredientDto dto);

    // ===== TO DTO =====
    @Override
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientLibelle", source = "ingredient.libelle")
    @Mapping(target = "alerte", expression = "java(entity.getQuantite().compareTo(entity.getSeuilAlerte()) < 0)")
    public abstract StockIngredientDto toDto(StockIngredient entity);

    // ===== PUT: full update =====
    public abstract void update(StockIngredientDto dto, @MappingTarget StockIngredient entity);

    // ===== PATCH: partial update (nulls are ignored) =====
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void patch(StockIngredientDto dto, @MappingTarget StockIngredient entity);

}