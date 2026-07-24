package com.boulangerie.production.internal;

import com.boulangerie.production.api.DestinationBoutiqueDto;
import com.boulangerie.production.api.ProductionAllocationApi;
import com.boulangerie.production.api.AllocationDetails;
import com.boulangerie.production.model.CanalDistribution;
import com.boulangerie.production.model.DestinationProduction;
import com.boulangerie.production.repository.DestinationProductionRepository;
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
    public DestinationBoutiqueDto getDestinationBoutique(Long produitId) {

        DestinationProduction destination = chargerDestinationBoutique(produitId);

        return new DestinationBoutiqueDto(
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
    public boolean verifierDisponibiliteBoutique(Long produitId, BigDecimal quantite) {
        DestinationProduction destination = chargerDestinationBoutique(produitId);
        return destination.getQuantiteDisponible()
                .compareTo(quantite) >= 0;
    }

    @Override
    public void enregistrerVenteBoutique(Long produitId, BigDecimal quantite) {

        DestinationProduction destination = chargerDestinationBoutique(produitId);
        destination.enregistrerVente(quantite);
        repository.save(destination);
    }

    private DestinationProduction chargerDestinationBoutique(Long produitId) {

        return repository.findFirstByProduitIdAndCanalOrderByDateDesc(
                        produitId,
                        CanalDistribution.BOUTIQUE)
                .orElseThrow(() ->
                        new EntityNotFoundException("Aucune destination BOUTIQUE trouvée pour le produit "
                                        + produitId));
    }
}