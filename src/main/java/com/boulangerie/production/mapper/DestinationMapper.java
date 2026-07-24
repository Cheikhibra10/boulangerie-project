// production/mapper/DestinationMapper.java
package com.boulangerie.production.mapper;

import com.boulangerie.administration.model.Livreur;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.repository.LivreurRepository;
import com.boulangerie.administration.repository.ProduitRepository;
import com.boulangerie.production.dto.DestinationDto;
import com.boulangerie.production.dto.DestinationRequestDto;
import com.boulangerie.production.model.DestinationProduction;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class DestinationMapper implements EntityMapper<DestinationDto, DestinationProduction> {


    // ===== TO ENTITY =====
    @Mapping(target = "livreurId", ignore = true)
    @Mapping(target = "lot", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract DestinationProduction toEntity(DestinationRequestDto dto);

    // ===== TO DTO =====
    @Override
    @Mapping(target = "livreurId", source = "livreurId")
    public abstract DestinationDto toDto(DestinationProduction entity);

    // ===== REQUEST TO ENTITY =====
    @Mapping(target = "lot", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "date", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract DestinationProduction requestToEntity(DestinationRequestDto dto, Long produitId);

    // ===== PUT =====
    public abstract void update(DestinationDto dto, @MappingTarget DestinationProduction entity);

    // ===== PATCH =====
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void patch(DestinationDto dto, @MappingTarget DestinationProduction entity);

}