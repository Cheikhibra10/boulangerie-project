package com.boulangerie.administration.service;

import com.boulangerie.administration.model.Fournisseur;
import com.boulangerie.administration.repository.FournisseurRepository;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FournisseurAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final FournisseurRepository fournisseurRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }
        var spec = SearchSpecifications.<Fournisseur>activeAndSearchByMultipleFields(q.trim().toLowerCase(), "nom", "telephone");
        return fournisseurRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(f -> AutocompleteItemDto.of(f.getId(), f.getNom(), f.getTelephone()))
                .toList();
    }
}

