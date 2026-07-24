// caisse/mapper/CaisseMapper.java
package com.boulangerie.comptabilite.mapper;

import com.boulangerie.comptabilite.dto.CaisseDto;
import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public abstract class CaisseMapper implements EntityMapper<CaisseDto, Caisse> {

    // ===== TO ENTITY =====
    @Override
    @Mapping(target = "ouvertePar", ignore = true)
    @Mapping(target = "fermeePar", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract Caisse toEntity(CaisseDto dto);

    // ===== TO DTO =====
    @Override
    @Mapping(target = "ouverteParId", source = "ouvertePar.id")
    @Mapping(target = "ouverteParNom", source = "ouvertePar.nom")
    @Mapping(target = "fermeeParId", source = "fermeePar.id")
    @Mapping(target = "fermeeParNom", source = "fermeePar.nom")
    public abstract CaisseDto toDto(Caisse entity);

    // ===== PUT: full update (nulls are explicitly set) =====
    public abstract void update(CaisseDto dto, @MappingTarget Caisse entity);

    // ===== PATCH: partial update (nulls are ignored) =====
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void patch(CaisseDto dto, @MappingTarget Caisse entity);


}