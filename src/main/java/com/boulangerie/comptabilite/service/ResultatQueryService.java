package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.ResultatDto;
import com.boulangerie.comptabilite.exception.ResultatNotFoundException;
import com.boulangerie.comptabilite.mapper.ResultatMapper;
import com.boulangerie.comptabilite.model.ResultatPeriode;
import com.boulangerie.comptabilite.repository.ResultatPeriodeRepository;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ResultatQueryService {
    
    private final ResultatPeriodeRepository resultatRepository;
    private final ResultatMapper resultatMapper;

    public ResultatDto getResultat(Long periodeId) {
        return resultatRepository.findByPeriodeId(periodeId)
                .map(resultatMapper::toDto)
                .orElseThrow(() -> new ResultatNotFoundException(periodeId));
    }
    
    public PageResponse<ResultatDto> getResultats(int page, int size) {
        Page<ResultatPeriode> pageResult = resultatRepository.findAllWithPeriode(
                PageRequest.of(page, size)
        );
        return PageUtils.toPageResponse(pageResult.map(resultatMapper::toDto));
    }

}