// abonnements/mapper/AbonnementMapper.java
package com.boulangerie.abonnements.mapper;

import com.boulangerie.abonnements.dto.AbonnementDto;
import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.abonnements.model.LigneAbonnement;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {LigneAbonnementMapper.class})
public interface AbonnementMapper extends EntityMapper<AbonnementDto, Abonnement> {

    @Override
    @Mapping(target = "livreurId", source = "livreurId")
    @Mapping(target = "lignes", source = "lignes")
    @Mapping(target = "caTotal", expression = "java(entity.getChiffreAffaires())")
    AbonnementDto toDto(Abonnement entity);

}