// livreurs/mapper/LigneCompteRenduMapper.java
package com.boulangerie.livreurs.mapper;

import com.boulangerie.livreurs.dto.LigneCompteRenduDto;
import com.boulangerie.livreurs.dto.LigneCompteRenduRequestDto;
import com.boulangerie.livreurs.model.CompteLivreurJournalier;
import com.boulangerie.livreurs.model.LigneCompteLivreur;
import com.boulangerie.production.model.DestinationProduction;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.math.BigDecimal;

@Mapper(componentModel = "spring")
public interface LigneCompteRenduMapper extends EntityMapper<LigneCompteRenduDto, LigneCompteLivreur> {

    @Override
    @Mapping(target = "destinationId", source = "destinationId")
    @Mapping(target = "montantCommission", source = "montantCommission")
    @Mapping(target = "montantApresDeduction", source = "montantApresDeduction")
    LigneCompteRenduDto toDto(LigneCompteLivreur entity);



        @Mapping(target = "id", ignore = true)
        @Mapping(target = "journalier", ignore = true)
        @Mapping(target = "destinationId", ignore = true)
        @Mapping(target = "qteNette", ignore = true)
        @Mapping(target = "montantCommission", ignore = true)
        @Mapping(target = "montantApresDeduction", ignore = true)
        LigneCompteLivreur toEntity(LigneCompteRenduRequestDto dto);

}