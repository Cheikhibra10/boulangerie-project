// livreurs/mapper/CompteRenduMapper.java
package com.boulangerie.livreurs.mapper;

import com.boulangerie.administration.model.Livreur;
import com.boulangerie.livreurs.dto.CompteRenduDto;
import com.boulangerie.livreurs.model.CompteLivreurJournalier;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.*;

import java.math.BigDecimal;

@Mapper(componentModel = "spring", uses = {LigneCompteRenduMapper.class})
public interface CompteRenduMapper extends EntityMapper<CompteRenduDto, CompteLivreurJournalier> {

    @Override
    @Mapping(target = "livreurId", source = "livreurId")
    @Mapping(target = "lignes", source = "lignes")
    @Mapping(target = "totalAVerser", source = "totalAVerser")
    @Mapping(target = "reliquatFin", source = "reliquatFin")
    @Mapping(target = "versementsDuJour", expression = "java(entity.getVersementsDuJour())")
    CompteRenduDto toDto(CompteLivreurJournalier entity);

    // Pour la création à partir d'un livreur
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "livreurId", source = "livreurId")
    @Mapping(target = "versement", ignore = true)
    @Mapping(target = "date", expression = "java(java.time.LocalDate.now())")
    @Mapping(target = "reliquatReport", source = "reliquatReport")
    @Mapping(target = "totalAVerser", ignore = true)
    @Mapping(target = "reliquatFin", ignore = true)
    @Mapping(target = "statut", constant = "BROUILLON")
    @Mapping(target = "lignes", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    CompteLivreurJournalier toRequestEntity(Long livreurId, BigDecimal reliquatReport);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(CompteRenduDto dto, @MappingTarget CompteLivreurJournalier entity);
}