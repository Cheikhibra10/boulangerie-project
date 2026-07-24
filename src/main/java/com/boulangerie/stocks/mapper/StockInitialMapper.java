// stocks/mapper/StockInitialMapper.java
package com.boulangerie.stocks.mapper;

import com.boulangerie.shared.mapper.EntityMapper;
import com.boulangerie.stocks.dto.StockInitialDto;
import com.boulangerie.stocks.model.StockInitial;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public abstract class StockInitialMapper implements EntityMapper<StockInitialDto, StockInitial> {

    // ===== TO ENTITY =====
    @Override
    @Mapping(target = "ingredient", ignore = true)
    @Mapping(target = "periodeId", ignore = true)
    @Mapping(target = "coutMoyenPondere", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract StockInitial toEntity(StockInitialDto dto);

    // ===== TO DTO =====
    @Override
    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientLibelle", source = "ingredient.libelle")
    @Mapping(target = "periodeId", source = "periodeId")
    public abstract StockInitialDto toDto(StockInitial entity);

}