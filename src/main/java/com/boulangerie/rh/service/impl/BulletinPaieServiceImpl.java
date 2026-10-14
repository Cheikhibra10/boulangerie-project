package com.boulangerie.rh.service.impl;

import com.boulangerie.comptabilite.api.PeriodeLookupApi;
import com.boulangerie.rh.dto.BulletinPaieDto;
import com.boulangerie.rh.dto.BulletinPaieFilter;
import com.boulangerie.rh.dto.GenererBulletinRequest;
import com.boulangerie.rh.mapper.BulletinPaieMapper;
import com.boulangerie.rh.model.*;
import com.boulangerie.rh.repository.AbsenceRepository;
import com.boulangerie.rh.repository.AvanceSalaireRepository;
import com.boulangerie.rh.repository.BulletinPaieRepository;
import com.boulangerie.rh.repository.ContratRepository;
import com.boulangerie.rh.repository.EmployeRepository;
import com.boulangerie.rh.service.BulletinPaieService;
import com.boulangerie.rh.specification.BulletinPaieSpecifications;
import com.boulangerie.shared.exception.BadRequestException;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class BulletinPaieServiceImpl implements BulletinPaieService {

    private static final BigDecimal JOURS_OUVRABLES_PAR_MOIS = new BigDecimal("26");

    private final BulletinPaieRepository bulletinPaieRepository;
    private final EmployeRepository employeRepository;
    private final ContratRepository contratRepository;
    private final AbsenceRepository absenceRepository;
    private final AvanceSalaireRepository avanceSalaireRepository;
    private final PeriodeLookupApi periodeLookupApi;
    private final BulletinPaieMapper bulletinPaieMapper;

    @Override
    public BulletinPaieDto generer(GenererBulletinRequest request) {
        Employe employe = employeRepository.findById(request.employeId())
                .orElseThrow(() -> new EntityNotFoundException("Employé introuvable"));

        if (bulletinPaieRepository.existsByEmployeIdAndPeriodeId(
                employe.getId(), request.periodeId())) {
            throw new BadRequestException(
                    "Un bulletin existe déjà pour cet employé sur cette période.");
        }

        PeriodeLookupApi.PeriodeInfo periode =
                periodeLookupApi.getByIdOrThrow(request.periodeId());

        // Contract must be ACTIF and cover the accounting period
        Contrat contrat = contratRepository
                .findActifCouvrantPeriode(
                        employe.getId(),
                        StatutContrat.ACTIF,
                        periode.dateDebut(),
                        periode.dateFin()
                )
                .orElseThrow(() -> new BadRequestException(
                        "Aucun contrat actif ne couvre la période du %s au %s pour %s."
                                .formatted(
                                        periode.dateDebut(),
                                        periode.dateFin(),
                                        employe.getNomComplet()
                                )
                ));

        // Defense in depth (query already filters; domain rule stays explicit)
        if (!contrat.couvre(periode.dateDebut(), periode.dateFin())) {
            throw new BadRequestException(
                    "Le contrat (du %s au %s) ne couvre pas la période du %s au %s."
                            .formatted(
                                    contrat.getDateDebut(),
                                    contrat.getDateFin() != null ? contrat.getDateFin() : "…",
                                    periode.dateDebut(),
                                    periode.dateFin()
                            )
            );
        }

        BigDecimal joursAbsents = calculerJoursAbsents(
                employe.getId(), periode.dateDebut(), periode.dateFin());

        BigDecimal joursTravailles = JOURS_OUVRABLES_PAR_MOIS.subtract(joursAbsents);
        if (joursTravailles.signum() < 0) {
            joursTravailles = BigDecimal.ZERO;
        }

        List<AvanceSalaire> avances = avanceSalaireRepository
                .findByEmployeIdAndStatut(employe.getId(), StatutAvance.APPROUVEE);

        BigDecimal totalAvances = avances.stream()
                .map(AvanceSalaire::getMontant)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal retenuesManuelles = request.retenues() != null
                ? request.retenues()
                : BigDecimal.ZERO;

        BigDecimal retenues = retenuesManuelles.add(totalAvances);

        BulletinPaie bulletin = new BulletinPaie()
                .setEmploye(employe)
                .setContrat(contrat)
                .setPeriodeId(periode.id())
                .setSalaireBrut(contrat.getSalaireBase())
                .setRetenues(retenues)
                .setJoursAbsents(joursAbsents)
                .setJoursTravailles(joursTravailles)
                .setStatut(StatutBulletin.BROUILLON);

        bulletin.recalculerNet();
        BulletinPaie saved = bulletinPaieRepository.save(bulletin);

        for (AvanceSalaire avance : avances) {
            avance.marquerDeduite(saved.getId());
        }

        return toDtoWithPeriode(saved, periode);
    }

    @Override
    @Transactional(readOnly = true)
    public BulletinPaieDto getById(Long id) {
        BulletinPaie bulletin = findOrThrow(id);
        PeriodeLookupApi.PeriodeInfo periode =
                periodeLookupApi.getByIdOrThrow(bulletin.getPeriodeId());
        return toDtoWithPeriode(bulletin, periode);
    }

    @Override
    public BulletinPaieDto valider(Long id) {
        BulletinPaie bulletin = findOrThrow(id);
        bulletin.valider();
        return getById(bulletin.getId());
    }

    @Override
    public BulletinPaieDto marquerPaye(Long id, LocalDate datePaiement) {
        BulletinPaie bulletin = findOrThrow(id);
        bulletin.marquerPaye(datePaiement);
        return getById(bulletin.getId());
    }

    @Override
    public BulletinPaieDto annuler(Long id) {
        BulletinPaie bulletin = findOrThrow(id);
        bulletin.annuler();
        return getById(bulletin.getId());
    }

    @Override
    public void delete(Long id) {
        BulletinPaie bulletin = findOrThrow(id);
        if (bulletin.getStatut() == StatutBulletin.PAYE
                || bulletin.getStatut() == StatutBulletin.VALIDE) {
            throw new BadRequestException(
                    "Un bulletin validé ou payé ne peut pas être supprimé.");
        }
        bulletinPaieRepository.delete(bulletin);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BulletinPaieDto> search(BulletinPaieFilter filter, Pageable pageable) {
        List<Long> employeIds = null;
        if (filter.employeNom() != null && !filter.employeNom().isBlank()) {
            employeIds = employeRepository.findIdsByNomOrPrenomOrMatricule(filter.employeNom());
            if (employeIds.isEmpty()) {
                return Page.empty(pageable);
            }
        }

        return bulletinPaieRepository
                .findAll(BulletinPaieSpecifications.withFilters(filter, employeIds), pageable)
                .map(b -> {
                    PeriodeLookupApi.PeriodeInfo p =
                            periodeLookupApi.getByIdOrThrow(b.getPeriodeId());
                    return toDtoWithPeriode(b, p);
                });
    }

    // ── helpers ──────────────────────────────────────────────

    private BigDecimal calculerJoursAbsents(
            Long employeId,
            LocalDate debut,
            LocalDate fin
    ) {
        List<Absence> absences =
                absenceRepository.findApprouveesOverlapping(employeId, StatutAbsence.APPROUVEE, debut, fin);

        long total = 0;
        for (Absence a : absences) {
            LocalDate from = a.getDateDebut().isBefore(debut) ? debut : a.getDateDebut();
            LocalDate to = a.getDateFin().isAfter(fin) ? fin : a.getDateFin();
            total += compterJoursOuvres(from, to);
        }
        return BigDecimal.valueOf(total);
    }

    private long compterJoursOuvres(LocalDate from, LocalDate to) {
        long count = 0;
        for (LocalDate d = from; !d.isAfter(to); d = d.plusDays(1)) {
            if (d.getDayOfWeek() != DayOfWeek.SUNDAY) {
                count++;
            }
        }
        return count;
    }

    private BulletinPaieDto toDtoWithPeriode(
            BulletinPaie bulletin,
            PeriodeLookupApi.PeriodeInfo periode
    ) {
        return bulletinPaieMapper.toDto(
                bulletin,
                periode.dateDebut(),
                periode.dateFin()
        );
    }

    private BulletinPaie findOrThrow(Long id) {
        return bulletinPaieRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Bulletin de paie introuvable: " + id));
    }
}