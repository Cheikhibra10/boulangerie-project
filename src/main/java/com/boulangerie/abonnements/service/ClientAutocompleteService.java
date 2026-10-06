package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.model.Client;
import com.boulangerie.abonnements.repository.ClientRepository;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.specification.SearchSpecifications;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClientAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final ClientRepository clientRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }
        var spec = SearchSpecifications.<Client>activeAndSearchByMultipleFields(q.trim().toLowerCase(), "nom", "prenom", "telephone");
        return clientRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(c -> AutocompleteItemDto.of(c.getId(), c.getNom() + " " + c.getPrenom(), c.getTelephone()))
                .toList();
    }
}

