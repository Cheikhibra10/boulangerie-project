package com.boulangerie.rh.mapper;

import com.boulangerie.rh.dto.CreateEmployeRequest;
import com.boulangerie.rh.dto.EmployeDto;
import com.boulangerie.rh.dto.UpdateEmployeRequest;
import com.boulangerie.rh.model.Employe;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring")
public interface EmployeMapper {

    EmployeDto toDto(Employe employe);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "matricule", ignore = true)
    @Mapping(target = "dateSortie", ignore = true)
    @Mapping(target = "statut", ignore = true)
    Employe toEntity(CreateEmployeRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "matricule", ignore = true)
    @Mapping(target = "dateEmbauche", ignore = true)
    void updateEntity(UpdateEmployeRequest request, @MappingTarget Employe employe);
}