// administration/mapper/UtilisateurMapper.java
package com.boulangerie.administration.mapper;

import com.boulangerie.administration.dto.UtilisateurDto;
import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public abstract class UtilisateurMapper implements EntityMapper<UtilisateurDto, Utilisateur> {


    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract Utilisateur toEntity(UtilisateurDto dto);

    @Override
    public abstract UtilisateurDto toDto(Utilisateur entity);

    @Override
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract void partialUpdate(@MappingTarget Utilisateur entity, UtilisateurDto dto);
}