// stocks/mapper/MouvementStockMapper.java
package com.boulangerie.stocks.mapper;

import com.boulangerie.shared.mapper.EntityMapper;
import com.boulangerie.stocks.dto.MouvementStockDto;
import com.boulangerie.stocks.model.MouvementStock;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MouvementStockMapper extends EntityMapper<MouvementStockDto, MouvementStock> {

    // ===== TO ENTITY =====
    @Override
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    MouvementStock toEntity(MouvementStockDto dto);

    // ===== TO DTO =====
    @Override
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientLibelle", source = "ingredient.libelle")
    MouvementStockDto toDto(MouvementStock entity);
}