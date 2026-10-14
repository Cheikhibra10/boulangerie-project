package com.boulangerie.rh.service;

import com.boulangerie.rh.dto.*;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ContratService {

    ContratDto create(CreateContratRequest request);

    ContratDto getById(Long id);

    ContratDto update(Long id, UpdateContratRequest request);

    ContratDto resilier(Long id, LocalDate dateFinEffective);

    Page<ContratDto> search(ContratFilter filter, Pageable pageable);

    List<AutocompleteItemDto> autocomplete(String q);
}