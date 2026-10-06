package com.boulangerie.achats.service;

import com.boulangerie.achats.model.Achat;
import com.boulangerie.achats.repository.AchatRepository;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AchatAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final AchatRepository achatRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }
        var spec = SearchSpecifications.<Achat>searchByField("fournisseur.nom", q.trim().toLowerCase());
        return achatRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(a -> AutocompleteItemDto.of(
                        a.getId(),
                        "Achat #" + a.getId(),
                        a.getFournisseur() != null ? a.getFournisseur().getNom() : null))
                .toList();
    }
}

