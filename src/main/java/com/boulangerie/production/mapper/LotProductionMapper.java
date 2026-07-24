// production/mapper/LotProductionMapper.java
package com.boulangerie.production.mapper;

import com.boulangerie.production.dto.LotProductionDto;
import com.boulangerie.production.model.LotProduction;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {DestinationMapper.class})
public abstract class LotProductionMapper implements EntityMapper<LotProductionDto, LotProduction> {



    @Override
    @Mapping(target = "produitId", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract LotProduction toEntity(LotProductionDto dto);

    @Mapping(target = "recetteId", source = "recetteId")
    @Mapping(target = "ecart", ignore = true)
    public abstract LotProductionDto toDto(LotProduction entity);

    @AfterMapping
    protected void computeEcart(LotProduction entity, @MappingTarget LotProductionDto dto) {

        BigDecimal prevue = entity.getQuantitePrevue();
        BigDecimal realisee = entity.getQuantiteRealisee() == null
                ? BigDecimal.ZERO
                : entity.getQuantiteRealisee();
        dto.setEcart(prevue.subtract(realisee));
    }
    // ===== PUT =====
    public abstract void update(LotProductionDto dto, @MappingTarget LotProduction entity);

    // ===== PATCH =====
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void patch(LotProductionDto dto, @MappingTarget LotProduction entity);


}