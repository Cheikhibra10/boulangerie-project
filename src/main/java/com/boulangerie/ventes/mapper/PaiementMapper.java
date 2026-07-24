package com.boulangerie.ventes.mapper;

import com.boulangerie.shared.mapper.EntityMapper;
import com.boulangerie.ventes.dto.PaiementDto;
import com.boulangerie.ventes.model.Paiement;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaiementMapper extends EntityMapper<PaiementDto, Paiement> {

    @Mapping(target = "venteId", source = "vente.id")
    PaiementDto toDto(Paiement entity);
}