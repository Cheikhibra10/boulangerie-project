package com.boulangerie.administration.mapper;

import com.boulangerie.administration.dto.FournisseurDto;
import com.boulangerie.administration.model.Fournisseur;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface FournisseurMapper extends EntityMapper<FournisseurDto, Fournisseur> {
}