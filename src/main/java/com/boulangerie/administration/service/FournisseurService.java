package com.boulangerie.administration.service;

import com.boulangerie.administration.dto.FournisseurDto;
import com.boulangerie.administration.dto.FournisseurFilter;
import com.boulangerie.administration.model.Fournisseur;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.service.DefaultService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FournisseurService extends DefaultService<Fournisseur, FournisseurDto> {
    Page<FournisseurDto> search(FournisseurFilter filter, Pageable pageable);

    List<AutocompleteItemDto> autocomplete(String q);
    // Méthodes spécifiques si nécessaire
}