package com.boulangerie.production.service;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.repository.ProduitRepository;
import com.boulangerie.production.model.LotProduction;
import com.boulangerie.production.repository.LotProductionRepository;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LotProductionAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final LotProductionRepository lotProductionRepository;
    private final ProduitRepository produitRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }
        var spec = SearchSpecifications.<LotProduction>searchByField("date", q.trim().toLowerCase());
        return lotProductionRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(l -> AutocompleteItemDto.of(
                        l.getId(),
                        "Lot #" + l.getId() + " - " + l.getDate(),
                        l.getProduitId() != null
                                ? produitRepository.findById(l.getProduitId()).map(Produit::getLibelle).orElse(null)
                                : null))
                .toList();
    }
}

