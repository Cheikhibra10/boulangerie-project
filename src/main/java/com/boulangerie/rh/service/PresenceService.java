package com.boulangerie.rh.service;

import com.boulangerie.rh.dto.CreatePresenceRequest;
import com.boulangerie.rh.dto.PresenceDto;
import com.boulangerie.rh.dto.PresenceFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PresenceService {

    PresenceDto create(CreatePresenceRequest request);

    PresenceDto getById(Long id);

    Page<PresenceDto> search(PresenceFilter filter, Pageable pageable);
}