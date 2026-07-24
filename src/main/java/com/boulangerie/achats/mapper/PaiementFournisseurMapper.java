package com.boulangerie.achats.mapper;

import com.boulangerie.achats.dto.PaiementFournisseurDto;
import com.boulangerie.achats.model.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PaiementFournisseurMapper {

    @Mapping(target = "montantPaye", source = "achat.montantPaye")
    @Mapping(target = "restantDu", source = "achat.restantDu")
    PaiementFournisseurDto toDto(PaiementFournisseur paiement);
}