package com.boulangerie.abonnements.mapper;

import com.boulangerie.abonnements.dto.ClientDto;
import com.boulangerie.abonnements.model.Client;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ClientMapper {


    @Mapping(target = "id", source = "id")
    ClientDto toDto(Client entity);

}