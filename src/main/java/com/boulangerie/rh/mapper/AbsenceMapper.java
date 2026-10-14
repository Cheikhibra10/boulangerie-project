package com.boulangerie.rh.mapper;

import com.boulangerie.rh.dto.AbsenceDto;
import com.boulangerie.rh.dto.CreateAbsenceRequest;
import com.boulangerie.rh.model.Absence;
import com.boulangerie.rh.model.Employe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AbsenceMapper {

    @Mapping(target = "employeId", source = "employe.id")
    @Mapping(target = "employeNomComplet", expression = "java(absence.getEmploye().getNomComplet())")
    AbsenceDto toDto(Absence absence);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employe", ignore = true)
    @Mapping(target = "statut", ignore = true)
    Absence toEntity(CreateAbsenceRequest request);

    default Absence toEntity(CreateAbsenceRequest request, Employe employe) {
        Absence absence = toEntity(request);
        absence.setEmploye(employe);
        return absence;
    }
}