package com.boulangerie.comptabilite.mapper;

import com.boulangerie.comptabilite.dto.ResultatDto;
import com.boulangerie.comptabilite.model.ResultatPeriode;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ResultatMapper {

    @Mapping(target = "periodeId", source = "periode.id")
    @Mapping(target = "caTotal", expression = "java(resultat.getCaTotal())")
    @Mapping(target = "beneficeBrut", expression = "java(resultat.getBeneficeBrut())")
    @Mapping(target = "beneficeDistribuable", expression = "java(resultat.getBeneficeDistribuable())")
    @Mapping(target = "beneficeNet", expression = "java(resultat.getBeneficeNet())")
    ResultatDto toDto(ResultatPeriode resultat);
}