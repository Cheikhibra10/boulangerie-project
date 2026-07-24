// abonnements/mapper/LigneAbonnementMapper.java
package com.boulangerie.abonnements.mapper;

import com.boulangerie.abonnements.dto.ClientAbonnementDto;
import com.boulangerie.abonnements.dto.LigneAbonnementDto;
import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.abonnements.model.LigneAbonnement;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = ClientMapper.class)
public interface LigneAbonnementMapper extends EntityMapper<LigneAbonnementDto, LigneAbonnement> {

    @Override
    @Mapping(target = "clientId", source = "client.id")
    @Mapping(target = "client", source = "client")
    LigneAbonnementDto toDto(LigneAbonnement entity);
}