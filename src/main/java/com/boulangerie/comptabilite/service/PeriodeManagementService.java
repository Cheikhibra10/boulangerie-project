package com.boulangerie.comptabilite.service;

import com.boulangerie.comptabilite.dto.ComptabiliteStatistiquesDto;
import com.boulangerie.comptabilite.dto.CreationPeriodeDto;
import com.boulangerie.comptabilite.dto.PeriodeDto;
import com.boulangerie.comptabilite.exception.PeriodeNotFoundException;
import com.boulangerie.comptabilite.exception.PeriodeOverlapException;
import com.boulangerie.comptabilite.mapper.PeriodeMapper;
import com.boulangerie.comptabilite.model.Periode;
import com.boulangerie.administration.model.StatutPeriode;
import com.boulangerie.comptabilite.repository.PeriodeRepository;
import com.boulangerie.comptabilite.repository.ResultatPeriodeRepository;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BusinessException;
import com.boulangerie.shared.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class PeriodeManagementService {
    
    private final PeriodeRepository periodeRepository;
    private final ResultatPeriodeRepository resultatRepository;
    private final PeriodeMapper periodeMapper;
    @Transactional
    public PeriodeDto creerPeriode(CreationPeriodeDto dto) {
        // Validate overlapping periods
        boolean overlap = periodeRepository.existsOverlappingPeriods(
                dto.getDateDebut(), 
                dto.getDateFin()
        );
        if (overlap) {
            throw new PeriodeOverlapException(dto.getDateDebut(), dto.getDateFin());
        }
        if (periodeRepository.existsByDateDebut(dto.getDateDebut())) {
            throw new BusinessException("Une période existe déjà avec cette date de début");
        }
        // Create period using factory method
        Periode periode = Periode.nouvellePeriode(dto.getDateDebut(), dto.getDateFin());
        
        periode = periodeRepository.save(periode);
        log.info("Période créée: {} - {}", dto.getDateDebut(), dto.getDateFin());
        
        return periodeMapper.toDto(periode);
    }

    @Transactional(readOnly = true)
    public ComptabiliteStatistiquesDto getStatistiques() {
        long totalPeriodes = periodeRepository.count();
        long periodesOuvertes = periodeRepository.countByStatut(StatutPeriode.OUVERTE);
        long periodesFermees = periodeRepository.countByStatut(StatutPeriode.FERMEE);
        long periodesCloturees = periodeRepository.countByStatut(StatutPeriode.CLOTUREE);
        long totalResultats = resultatRepository.count();

        // Calculate financial statistics
        BigDecimal caTotal = resultatRepository.sumCaTotal();
        BigDecimal beneficeTotal = resultatRepository.sumBeneficeNet();
        BigDecimal beneficeMoyen = totalResultats > 0
                ? beneficeTotal.divide(BigDecimal.valueOf(totalResultats), 2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        // Get last closed period
        Optional<PeriodeDto> derniereCloturee = periodeRepository.findTopByStatutOrderByDateFinDesc(StatutPeriode.CLOTUREE)
                .map(periodeMapper::toDto);

        // Get current open period
        Optional<PeriodeDto> periodeEnCours = periodeRepository.findByStatut(StatutPeriode.OUVERTE)
                .map(periodeMapper::toDto);

        return ComptabiliteStatistiquesDto.builder()
                .totalPeriodes(totalPeriodes)
                .periodesOuvertes(periodesOuvertes)
                .periodesFermees(periodesFermees)
                .periodesCloturees(periodesCloturees)
                .totalResultats(totalResultats)
                .caTotal(caTotal)
                .beneficeTotal(beneficeTotal)
                .beneficeMoyen(beneficeMoyen)
                .dernierePeriodeCloturee(derniereCloturee.orElse(null))
                .periodeEnCours(periodeEnCours.orElse(null))
                .build();
    }

    @Transactional(readOnly = true)
    public PeriodeDto getPeriode(Long id) {
        return periodeRepository.findById(id)
                .map(periodeMapper::toDto)
                .orElseThrow(() -> new PeriodeNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Periode getPeriodeEntity(Long id) {
        return periodeRepository.findById(id)
                .orElseThrow(() -> new PeriodeNotFoundException(id));
    }
    
    @Transactional(readOnly = true)
    public PeriodeDto getPeriodeOuverte() {
        return periodeRepository.findByStatut(StatutPeriode.OUVERTE)
                .map(periodeMapper::toDto)
                .orElseThrow(() -> new BusinessException("Aucune période ouverte"));
    }
    
    @Transactional(readOnly = true)
    public PeriodeDto getPeriodeByDate(LocalDate date) {
        return periodeRepository.findByDate(date)
                .map(periodeMapper::toDto)
                .orElseThrow(() -> new BusinessException("Aucune période pour la date " + date));
    }
    
    @Transactional(readOnly = true)
    public PageResponse<PeriodeDto> getPeriodes(int page, int size) {
        Page<Periode> pageResult = periodeRepository.findAllByOrderByDateDebutDesc(
                PageRequest.of(page, size)
        );
        return PageUtils.toPageResponse(pageResult.map(periodeMapper::toDto));
    }

    @Transactional
    public PeriodeDto fermerPeriode(Long id) {
        Periode periode = getPeriodeEntity(id);
        periode.fermer();
        periode = periodeRepository.save(periode);
        log.info("Période {} fermée", id);
        return periodeMapper.toDto(periode);
    }

    @Transactional
    public PeriodeDto ouvrirPeriode(Long id) {
        Periode periode = getPeriodeEntity(id);
        periode.ouvrir();
        periode = periodeRepository.save(periode);
        log.info("Période {} ouverte", id);
        return periodeMapper.toDto(periode);
    }
}