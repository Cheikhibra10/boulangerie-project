// stocks/mapper/StockIngredientSnapshotMapper.java
package com.boulangerie.stocks.mapper;

import com.boulangerie.shared.mapper.EntityMapper;
import com.boulangerie.stocks.dto.StockIngredientSnapshotDto;
import com.boulangerie.stocks.model.StockIngredientSnapshot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface StockIngredientSnapshotMapper extends EntityMapper<StockIngredientSnapshotDto, StockIngredientSnapshot> {

    @Override
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "periodeId", ignore = true)
    StockIngredientSnapshot toEntity(StockIngredientSnapshotDto dto);

    @Override
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientLibelle", source = "ingredient.libelle")
    StockIngredientSnapshotDto toDto(StockIngredientSnapshot entity);
}