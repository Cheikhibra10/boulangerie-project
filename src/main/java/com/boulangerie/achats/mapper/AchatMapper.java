package com.boulangerie.achats.mapper;

import com.boulangerie.achats.dto.AchatDto;
import com.boulangerie.achats.dto.CreationAchatDto;
import com.boulangerie.achats.model.Achat;
import com.boulangerie.administration.mapper.FournisseurMapper;
import com.boulangerie.administration.model.Fournisseur;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring", uses = {LigneAchatMapper.class, PaiementFournisseurMapper.class, FournisseurMapper.class})
public interface AchatMapper {

    @Mapping(target = "fournisseurId", source = "fournisseur.id")
    @Mapping(target = "fournisseurNom", source = "fournisseur", qualifiedByName = "fournisseurNomComplet")
    @Mapping(target = "lignes", source = "lignes")
    @Mapping(target = "paiements", source = "paiementFournisseurs")
    AchatDto toDto(Achat achat);

    @Named("fournisseurNomComplet")
    default String fournisseurNomComplet(Fournisseur fournisseur) {
        return fournisseur.getPrenom() + " " + fournisseur.getNom();
    }

}