// stocks/mapper/StockProduitMapper.java
package com.boulangerie.stocks.mapper;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.repository.ProduitRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.mapper.EntityMapper;
import com.boulangerie.stocks.dto.StockProduitDto;
import com.boulangerie.stocks.model.StockProduit;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class StockProduitMapper implements EntityMapper<StockProduitDto, StockProduit> {

    // ===== TO ENTITY =====
    @Override
    @Mapping(target = "produit", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    public abstract StockProduit toEntity(StockProduitDto dto);

    // ===== TO DTO =====
    @Override
    @Mapping(target = "produitId", source = "produit.id")
    @Mapping(target = "produitLibelle", source = "produit.libelle")
    @Mapping(target = "alerte", expression = "java(entity.getQuantite().compareTo(entity.getSeuilAlerte()) < 0)")
    public abstract StockProduitDto toDto(StockProduit entity);

    // ===== PUT =====
    public abstract void update(StockProduitDto dto, @MappingTarget StockProduit entity);

    // ===== PATCH =====
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void patch(StockProduitDto dto, @MappingTarget StockProduit entity);


}