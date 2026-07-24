package com.boulangerie.ventes.mapper;

import com.boulangerie.stocks.dto.StockMovement;
import com.boulangerie.ventes.model.LigneVenteBoutique;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface StockMovementMapper {

    StockMovement toStockMovement(LigneVenteBoutique ligne);

    List<StockMovement> toStockMovements(List<LigneVenteBoutique> lignes);

}