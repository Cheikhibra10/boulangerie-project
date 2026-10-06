package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.abonnements.repository.AbonnementRepository;
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
public class AbonnementAutocompleteService {

    private static final int MAX_RESULTS = 15;

    private final AbonnementRepository abonnementRepository;
    private final LivreurRepository livreurRepository;

    public List<AutocompleteItemDto> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return Collections.emptyList();
        }
        var spec = SearchSpecifications.<Abonnement>activeAndSearchByField("nom", q.trim().toLowerCase());
        return abonnementRepository.findAll(spec, PageRequest.of(0, MAX_RESULTS))
                .stream()
                .map(a -> AutocompleteItemDto.of(
                        a.getId(),
                        "Abonnement #" + a.getId(),
                        a.getLivreurId() != null
                                ? livreurRepository.findById(a.getLivreurId())
                                .map(l -> l.getNom() + " " + l.getPrenom())
                                .orElse(null)
                                : null))
                .toList();
    }
}

