package com.boulangerie.livreurs.mapper;

import com.boulangerie.livreurs.dto.CommissionRegleDto;
import com.boulangerie.livreurs.model.CommissionRegle;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommissionRegleMapper extends EntityMapper<CommissionRegleDto, CommissionRegle> {

    CommissionRegleDto toDto(CommissionRegle entity);
}