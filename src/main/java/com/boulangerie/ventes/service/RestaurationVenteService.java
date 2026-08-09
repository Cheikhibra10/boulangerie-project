package com.boulangerie.ventes.service;

import com.boulangerie.production.api.ProductionAllocationApi;
import com.boulangerie.stocks.service.StockService;
import com.boulangerie.ventes.dto.LigneRetourRequestDto;
import com.boulangerie.ventes.mapper.StockMovementMapper;
import com.boulangerie.ventes.model.LigneVenteBoutique;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class RestaurationVenteService {

    private final StockService stockService;
    private final ProductionAllocationApi productionApi;
    private final StockMovementMapper stockMovementMapper;

    public void restaurerStock(List<LigneRetourRequestDto> retours) {
        stockService.incrementerStock(
                retours.stream()
                        .map(stockMovementMapper::toRetour)
                        .toList()
        );
    }

    public void restaurerStockAnnulation(List<LigneVenteBoutique> lignes) {
        stockService.incrementerStock(
                lignes.stream()
                        .map(stockMovementMapper::toStockMovement)
                        .toList()
        );
    }

    public void restaurerBoutique(List<LigneRetourRequestDto> retours) {
        retours.forEach(retour ->
                productionApi.retourner(
                        retour.getProduitId(),
                        retour.getQuantite()));
    }

    public void restaurerBoutiqueAnnulation(List<LigneVenteBoutique> lignes) {
        lignes.forEach(ligne ->
                productionApi.retourner(
                        ligne.getProduitId(),
                        ligne.getQuantite()));
    }
}