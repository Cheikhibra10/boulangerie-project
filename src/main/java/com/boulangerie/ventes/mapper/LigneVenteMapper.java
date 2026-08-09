// ventes/mapper/LigneVenteMapper.java
package com.boulangerie.ventes.mapper;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.repository.ProduitRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.mapper.EntityMapper;
import com.boulangerie.ventes.dto.LigneVenteDto;
import com.boulangerie.ventes.dto.LigneVenteRequestDto;
import com.boulangerie.ventes.model.LigneVenteBoutique;
import com.boulangerie.ventes.model.VenteBoutique;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.springframework.beans.factory.annotation.Autowired;

@Mapper(componentModel = "spring")
public abstract class LigneVenteMapper implements EntityMapper<LigneVenteDto, LigneVenteBoutique> {
    @Override
    @Mapping(target = "produitId", source = "produitId")
    @Mapping(target = "produitLibelle", source = "produitLibelle")
    @Mapping(target = "quantiteRetournee", source = "quantiteRetournee")
    @Mapping(target = "quantiteDisponibleRetour", expression = "java(entity.getQuantiteDisponibleRetour())"
    )
    @Mapping(target="total", expression = "java(entity.getTotal())")
    public abstract LigneVenteDto toDto(LigneVenteBoutique entity);

    @Mapping(target="id", ignore=true)
    @Mapping(target="vente", source="vente")
    @Mapping(target="produitId", ignore=true)
    @Mapping(target="createdAt", ignore=true)
    @Mapping(target="updatedAt", ignore=true)
    @Mapping(target="createdBy", ignore=true)
    @Mapping(target="updatedBy", ignore=true)
    public abstract LigneVenteBoutique requestToEntity(LigneVenteRequestDto dto, VenteBoutique vente);

    public abstract void update(LigneVenteDto dto, @MappingTarget LigneVenteBoutique entity);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    public abstract void patch(LigneVenteDto dto, @MappingTarget LigneVenteBoutique entity);
}