package com.boulangerie.production.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ProductionAllocationApi {

    AllocationDetails getAllocation(Long destinationId);
    BoutiqueStockDto getDestinationBoutique(Long produitId);
    void verifierDisponibiliteBoutique(Long produitId, BigDecimal quantite);
    void retourner(Long produitId, BigDecimal quantite);
    void vendre(Long produitId, BigDecimal quantite);
    List<Long> findDestinations(Long livreurId, LocalDate date);
}