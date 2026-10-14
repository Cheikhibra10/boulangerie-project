package com.boulangerie.rh.service;

import com.boulangerie.rh.dto.AbsenceDto;
import com.boulangerie.rh.dto.AbsenceFilter;
import com.boulangerie.rh.dto.CreateAbsenceRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

public interface AbsenceService {
    AbsenceDto create(CreateAbsenceRequest request);

    AbsenceDto getById(Long id);

    Page<AbsenceDto> search(AbsenceFilter filter, Pageable pageable);

    AbsenceDto approuver(Long id);

    AbsenceDto refuser(Long id, String motif);

    AbsenceDto annuler(Long id);
}
