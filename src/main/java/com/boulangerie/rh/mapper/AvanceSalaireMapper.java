package com.boulangerie.rh.mapper;

import com.boulangerie.rh.dto.AvanceSalaireDto;
import com.boulangerie.rh.dto.CreateAvanceRequest;
import com.boulangerie.rh.model.AvanceSalaire;
import com.boulangerie.rh.model.Employe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AvanceSalaireMapper {

    @Mapping(target = "employeId", source = "employe.id")
    @Mapping(target = "employeNomComplet", expression = "java(avance.getEmploye().getNomComplet())")
    AvanceSalaireDto toDto(AvanceSalaire avance);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employe", ignore = true)
    @Mapping(target = "statut", ignore = true)
    @Mapping(target = "bulletinId", ignore = true)
    AvanceSalaire toEntity(CreateAvanceRequest request);

    default AvanceSalaire toEntity(CreateAvanceRequest request, Employe employe) {
        AvanceSalaire avance = toEntity(request);
        avance.setEmploye(employe);
        return avance;
    }
}