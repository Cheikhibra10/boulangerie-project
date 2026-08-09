package com.boulangerie.comptabilite.mapper;

import com.boulangerie.comptabilite.dto.DepenseDto;
import com.boulangerie.comptabilite.model.DepensePeriode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface DepenseMapper {

    @Mapping(target = "id", source = "id")
    @Mapping(target = "categorieId", source = "categorie.id")
    @Mapping(target = "categorie", source = "categorie.libelle")
    @Mapping(target = "libelle", source = "mouvement.libelle")
    @Mapping(target = "montant", source = "mouvement.montant")
    @Mapping(target = "date", source = "mouvement.createdAt")
    @Mapping(target = "periodeId", source = "periode.id")
    DepenseDto toDto(DepensePeriode entity);
}