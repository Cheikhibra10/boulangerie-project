package com.boulangerie.ventes.service;

import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.specification.SearchSpecifications;
import com.boulangerie.ventes.model.VenteBoutique;
import com.boulangerie.ventes.repository.VenteBoutiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenteBoutiqueAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final VenteBoutiqueRepository venteBoutiqueRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }

        var spec = SearchSpecifications.<VenteBoutique>searchByMultipleFields(q.trim().toLowerCase(), "date", "statut");
        return venteBoutiqueRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(v -> AutocompleteItemDto.of(
                        v.getId(),
                        "Vente #" + v.getId(),
                        v.getStatut() != null ? v.getStatut().name() : null))
                .toList();
    }
}

