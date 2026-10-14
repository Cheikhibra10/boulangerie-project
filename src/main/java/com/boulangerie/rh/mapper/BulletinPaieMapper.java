package com.boulangerie.rh.mapper;

import com.boulangerie.rh.dto.BulletinPaieDto;
import com.boulangerie.rh.model.BulletinPaie;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.LocalDate;

@Mapper(componentModel = "spring")
public interface BulletinPaieMapper {

    @Mapping(target = "employeId", source = "bulletin.employe.id")
    @Mapping(target = "employeNomComplet", expression = "java(bulletin.getEmploye().getNomComplet())")
    @Mapping(target = "contratId", source = "bulletin.contrat.id")
    @Mapping(target = "periodeDebut", source = "periodeDebut")
    @Mapping(target = "periodeFin", source = "periodeFin")
    BulletinPaieDto toDto(BulletinPaie bulletin, LocalDate periodeDebut, LocalDate periodeFin);

    default BulletinPaieDto toDto(BulletinPaie bulletin) {
        return toDto(bulletin, null, null);
    }
}