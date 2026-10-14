package com.boulangerie.rh.mapper;

import com.boulangerie.rh.dto.CreatePresenceRequest;
import com.boulangerie.rh.dto.PresenceDto;
import com.boulangerie.rh.model.Employe;
import com.boulangerie.rh.model.Presence;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PresenceMapper {

    @Mapping(target = "employeId", source = "employe.id")
    @Mapping(target = "employeNomComplet", expression = "java(presence.getEmploye().getNomComplet())")
    PresenceDto toDto(Presence presence);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "employe", ignore = true)
    Presence toEntity(CreatePresenceRequest request);

    default Presence toEntity(CreatePresenceRequest request, Employe employe) {
        Presence presence = toEntity(request);
        presence.setEmploye(employe);
        return presence;
    }
}