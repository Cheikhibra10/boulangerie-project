package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.ComptabiliteStatistiquesDto;
import com.boulangerie.comptabilite.dto.CreationPeriodeDto;
import com.boulangerie.comptabilite.dto.PeriodeDto;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.shared.dto.PageResponse;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

public interface PeriodeManagementService {
    PeriodeDto creerPeriode(CreationPeriodeDto dto);

    PeriodeDto fermerPeriode(Long id);

    PeriodeDto ouvrirPeriode(Long id);

    PeriodeDto getPeriode(Long id);
    Periode findPeriodeOuverte();
    PeriodeDto getPeriodeOuverte();

    PeriodeDto getPeriodeByDate(LocalDate date);

    PageResponse<PeriodeDto> getPeriodes(int page, int size);

    ComptabiliteStatistiquesDto getStatistiques();
}
