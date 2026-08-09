package com.boulangerie.administration.mapper;

import com.boulangerie.administration.dto.ProduitDto;
import com.boulangerie.administration.dto.ProduitUpdateDto;
import com.boulangerie.administration.model.CategorieProduit;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.repository.CategorieProduitRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.*;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class ProduitMapper implements EntityMapper<ProduitDto, Produit> {

    @Override
    @Mapping(target = "categorie", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract Produit toEntity(ProduitDto dto);

    @Override
    @Mapping(target = "categorieId", source = "categorie.id")
    public abstract ProduitDto toDto(Produit entity);

    @BeanMapping(
            nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE
    )
    public abstract void partialUpdate(@MappingTarget Produit produit, ProduitUpdateDto dto);

}