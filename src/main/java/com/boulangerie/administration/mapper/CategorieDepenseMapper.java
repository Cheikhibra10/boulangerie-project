package com.boulangerie.administration.mapper;

import com.boulangerie.administration.dto.CategorieDepenseDto;
import com.boulangerie.administration.model.CategorieDepense;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategorieDepenseMapper extends EntityMapper<CategorieDepenseDto, CategorieDepense> {
}