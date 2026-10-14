package com.boulangerie.rh.service;

import com.boulangerie.rh.dto.CreateEmployeRequest;
import com.boulangerie.rh.dto.EmployeDto;
import com.boulangerie.rh.dto.EmployeFilter;
import com.boulangerie.rh.dto.UpdateEmployeRequest;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface EmployeService {
    EmployeDto create(CreateEmployeRequest request);

    EmployeDto getById(Long id);

    EmployeDto update(Long id, UpdateEmployeRequest request);

    Page<EmployeDto> search(EmployeFilter filter, Pageable pageable);

    List<AutocompleteItemDto> autocomplete(String q);
}
