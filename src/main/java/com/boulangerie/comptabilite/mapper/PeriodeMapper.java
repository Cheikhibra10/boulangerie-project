package com.boulangerie.comptabilite.mapper;

import com.boulangerie.comptabilite.dto.PeriodeDto;
import com.boulangerie.comptabilite.model.Periode;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface PeriodeMapper {
    PeriodeDto toDto(Periode periode);

}