package com.boulangerie.production.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface ProductionAllocationApi {

    AllocationDetails getAllocation(Long destinationId);
    DestinationBoutiqueDto getDestinationBoutique(Long produitId);
    boolean verifierDisponibiliteBoutique(Long produitId, BigDecimal quantite);
    void enregistrerVenteBoutique(Long produitId, BigDecimal quantite);
    List<Long> findDestinations(Long livreurId, LocalDate date);
}