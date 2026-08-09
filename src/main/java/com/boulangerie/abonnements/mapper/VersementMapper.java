package com.boulangerie.abonnements.mapper;

import com.boulangerie.abonnements.dto.VersementGlobalDto;
import com.boulangerie.abonnements.model.VersementAbonnement;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface VersementMapper extends EntityMapper<VersementGlobalDto, VersementAbonnement> {

    VersementGlobalDto toDto(VersementAbonnement entity);
}
