// administration/mapper/RecetteMapper.java
package com.boulangerie.administration.mapper;

import com.boulangerie.administration.dto.RecetteDto;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.model.Recette;
import com.boulangerie.administration.repository.ProduitRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.mapper.EntityMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring", uses = {RecetteIngredientMapper.class})
public abstract class RecetteMapper implements EntityMapper<RecetteDto, Recette> {


    @Override
    @Mapping(target = "produit", ignore = true)
    @Mapping(target = "ingredients", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract Recette toEntity(RecetteDto dto);

    @Override
    @Mapping(target = "produitId", source = "produit.id")
    public abstract RecetteDto toDto(Recette entity);
}