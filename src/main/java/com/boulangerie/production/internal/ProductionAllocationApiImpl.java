package com.boulangerie.production.internal;

import com.boulangerie.production.api.BoutiqueStockDto;
import com.boulangerie.production.api.ProductionAllocationApi;
import com.boulangerie.production.api.AllocationDetails;
import com.boulangerie.production.model.CanalDistribution;
import com.boulangerie.production.model.DestinationProduction;
import com.boulangerie.production.repository.DestinationProductionRepository;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
class ProductionAllocationApiImpl implements ProductionAllocationApi {

    private final DestinationProductionRepository repository;

    @Override
    public AllocationDetails getAllocation(Long id) {

        DestinationProduction destination = repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("allocation introuvable"));

        return new AllocationDetails(
                destination.getId(),
                destination.getProduitId(),
                destination.getQuantite(),
                destination.getLivreurId(),
                destination.getPrixUnitaire()
        );
    }

    @Override
    public List<Long> findDestinations(Long livreurId, LocalDate date) {

        return repository.findByDateAndLivreurId(date, livreurId)
                .stream()
                .map(DestinationProduction::getId)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public BoutiqueStockDto getDestinationBoutique(Long produitId) {

        DestinationProduction destination = chargerDestinationBoutique(produitId);

        return new BoutiqueStockDto(
                destination.getId(),
                destination.getProduitId(),
                destination.getQuantite(),
                destination.getQuantiteConsommee(),
                destination.getQuantiteDisponible(),
                destination.getPrixUnitaire()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public void verifierDisponibiliteBoutique(Long produitId, BigDecimal quantite) {
        DestinationProduction destination = chargerDestinationBoutique(produitId);
         if(quantite.compareTo(destination.getQuantiteDisponible()) >= 0) {
             throw new BadRequestException("Stock insuffisant pour la destination BOUTIQUE");
         }
    }

    private DestinationProduction chargerDestinationBoutique(Long produitId) {

        return repository.findFirstByProduitIdAndCanalOrderByDateDesc(
                        produitId,
                        CanalDistribution.BOUTIQUE)
                .orElseThrow(() ->
                        new EntityNotFoundException("Aucune destination BOUTIQUE trouvée pour le produit "
                                        + produitId));
    }

    @Override
    @Transactional
    public void vendre(Long produitId, BigDecimal quantite) {
        DestinationProduction destination = chargerDestinationBoutique(produitId);
        destination.enregistrerVente(quantite);
    }

    @Override
    @Transactional
    public void retourner(Long produitId, BigDecimal quantite) {
        DestinationProduction destination = chargerDestinationBoutique(produitId);
        destination.retourner(quantite);
    }
}