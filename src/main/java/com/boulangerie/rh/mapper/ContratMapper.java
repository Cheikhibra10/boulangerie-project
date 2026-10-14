package com.boulangerie.rh.mapper;

import com.boulangerie.rh.dto.*;
import com.boulangerie.rh.model.*;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface ContratMapper {

    @Mapping(target = "employeId", source = "employe.id")
    @Mapping(target = "employeNomComplet", expression = "java(contrat.getEmploye().getNomComplet())")
    ContratDto toDto(Contrat contrat);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employe", ignore = true)
    @Mapping(target = "statut", ignore = true)
    Contrat toEntity(CreateContratRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employe", ignore = true)
    @Mapping(target = "type", ignore = true)
    @Mapping(target = "dateDebut", ignore = true)
    void updateEntity(UpdateContratRequest request, @MappingTarget Contrat contrat);

    default Contrat toEntity(CreateContratRequest request, Employe employe) {
        Contrat c = toEntity(request);
        c.setEmploye(employe);
        return c;
    }
}

