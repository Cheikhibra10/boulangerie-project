package com.boulangerie.administration.mapper;

import com.boulangerie.administration.dto.CategorieProduitDto;
import com.boulangerie.administration.model.CategorieProduit;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategorieProduitMapper extends EntityMapper<CategorieProduitDto, CategorieProduit> {
}