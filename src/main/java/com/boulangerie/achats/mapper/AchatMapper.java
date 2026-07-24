package com.boulangerie.achats.mapper;

import com.boulangerie.achats.dto.AchatDto;
import com.boulangerie.achats.dto.CreationAchatDto;
import com.boulangerie.achats.model.Achat;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {LigneAchatMapper.class, PaiementFournisseurMapper.class})
public interface AchatMapper {

    @Mapping(target = "fournisseurId", source = "fournisseur.id")
    @Mapping(target = "fournisseurNom", source = "fournisseur.nom")
    @Mapping(target = "lignes", source = "lignes")
    @Mapping(target = "paiements", source = "paiementFournisseurs")
    AchatDto toDto(Achat achat);

}