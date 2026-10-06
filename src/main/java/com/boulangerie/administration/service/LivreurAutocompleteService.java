package com.boulangerie.administration.service;

import com.boulangerie.administration.model.Livreur;
import com.boulangerie.administration.repository.LivreurRepository;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LivreurAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final LivreurRepository livreurRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().length() < 2) {
            return Collections.emptyList();
        }
        var spec = SearchSpecifications.<Livreur>activeAndSearchByMultipleFields(q.trim().toLowerCase(), "nom", "prenom", "telephone");
        return livreurRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(l -> AutocompleteItemDto.of(l.getId(), l.getNom() + " " + l.getPrenom(), l.getTelephone()))
                .toList();
    }
}

