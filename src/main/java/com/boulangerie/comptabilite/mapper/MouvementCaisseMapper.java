// caisse/mapper/MouvementCaisseMapper.java
package com.boulangerie.comptabilite.mapper;

import com.boulangerie.comptabilite.dto.MouvementCaisseDto;
import com.boulangerie.comptabilite.model.MouvementCaisse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface MouvementCaisseMapper {


    @Mapping(target = "caisseId", source = "caisse.id")
    MouvementCaisseDto toDto(MouvementCaisse entity);
}