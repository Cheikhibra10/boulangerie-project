package com.boulangerie.administration.mapper;

import com.boulangerie.administration.dto.LivreurDto;
import com.boulangerie.administration.model.Livreur;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LivreurMapper extends EntityMapper<LivreurDto, Livreur> {

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    Livreur toEntity(LivreurDto dto);
}