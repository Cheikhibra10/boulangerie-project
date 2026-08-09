// caisse/mapper/MouvementCaisseMapper.java
package com.boulangerie.comptabilite.mapper;

import com.boulangerie.comptabilite.dto.MouvementCaisseDto;
import com.boulangerie.comptabilite.model.MouvementCaisse;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@Mapper(componentModel = "spring")
public abstract class MouvementCaisseMapper implements EntityMapper<MouvementCaisseDto, MouvementCaisse> {

    @Override
    @Mapping(target = "date", source = "createdAt")
    @Mapping(target = "caisseId", source = "caisse.id")
    public abstract MouvementCaisseDto toDto(MouvementCaisse entity);

    public LocalDate map(Instant createdAt) {
        return createdAt == null ? null : createdAt.atZone(ZoneId.systemDefault()).toLocalDate();
    }

}