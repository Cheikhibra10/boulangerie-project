package com.boulangerie.achats.mapper;

import com.boulangerie.achats.dto.LigneAchatDto;
import com.boulangerie.achats.dto.LigneAchatRequestDto;
import com.boulangerie.achats.model.LigneAchat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LigneAchatMapper {

    @Mapping(target = "ingredientId", source = "ingredient.id")
    @Mapping(target = "ingredientLibelle", source = "ingredient.libelle")
    @Mapping(target = "quantiteAcceptee", expression = "java(ligne.getQuantiteAcceptee())")
    @Mapping(target = "ecart", expression = "java(ligne.getEcart())")
    @Mapping(target = "montantCommande", expression = "java(ligne.getMontantCommande())")
    @Mapping(target = "montantRecu", expression = "java(ligne.getMontantAccepte())")
    LigneAchatDto toDto(LigneAchat ligne);
}