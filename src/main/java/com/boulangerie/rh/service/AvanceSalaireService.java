package com.boulangerie.rh.service;

import com.boulangerie.rh.dto.AvanceSalaireDto;
import com.boulangerie.rh.dto.AvanceSalaireFilter;
import com.boulangerie.rh.dto.CreateAvanceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AvanceSalaireService {

    AvanceSalaireDto create(CreateAvanceRequest request);

    AvanceSalaireDto getById(Long id);

    AvanceSalaireDto approuver(Long id);

    AvanceSalaireDto refuser(Long id);

    AvanceSalaireDto annuler(Long id);

    Page<AvanceSalaireDto> search(AvanceSalaireFilter filter, Pageable pageable);
}