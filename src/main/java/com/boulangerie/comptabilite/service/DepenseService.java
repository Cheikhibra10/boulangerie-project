package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.DepenseDto;
import com.boulangerie.comptabilite.dto.EnregistrerDepenseDto;
import com.boulangerie.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

public interface DepenseService {

    DepenseDto enregistrerDepense(EnregistrerDepenseDto dto);

    DepenseDto getDepense(Long id);


    PageResponse<DepenseDto> getDepenses(
            Long categorieId,
            Long periodeId,
            int page,
            int size);
}