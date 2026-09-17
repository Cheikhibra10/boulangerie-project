package com.boulangerie.abonnements.internal;

import com.boulangerie.abonnements.api.AbonnementStatisticsApi;
import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.abonnements.projection.ConsommationReportProjection;
import com.boulangerie.abonnements.projection.LigneAbonnementReportProjection;
import com.boulangerie.abonnements.projection.PaiementReportProjection;
import com.boulangerie.abonnements.repository.AbonnementRepository;
import com.boulangerie.abonnements.repository.ConsommationJournaliereRepository;
import com.boulangerie.abonnements.repository.LigneAbonnementRepository;
import com.boulangerie.abonnements.repository.PaiementAbonnementRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
class AbonnementStatisticsApiImpl implements AbonnementStatisticsApi {

    private final AbonnementRepository repository;
    private final ConsommationJournaliereRepository consommationRepository;
    private final LigneAbonnementRepository ligneAbonnementRepository;
    private final PaiementAbonnementRepository paiementRepository;

    @Override
    public BigDecimal calculerCA(LocalDate debut, LocalDate fin) {
        return repository.sumCaBetweenDates(debut, fin);
    }

    @Override
    public Integer calculerNActif() {
        return repository.countByActifTrue();
    }

    @Override
    public BigDecimal calculerCC() {
        return repository.sumCreditsClients();
    }

    @Override
    public Abonnement getAbonnement(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Abonnement introuvable : " + id));
    }

    @Override
    public List<Abonnement> findAbonnementsActifsPourPeriode(LocalDate debut, LocalDate fin) {
        return repository.findAbonnementsActifsPourPeriode(debut, fin);
    }

    @Override
    public List<ConsommationReportProjection> getReportData(List<Long> abonnementIds, LocalDate debut, LocalDate fin) {
        return consommationRepository.findReportData(abonnementIds, debut, fin);
    }

    @Override
    public List<ConsommationReportProjection> getReportData(Long abonnementId, LocalDate debut, LocalDate fin) {
        return consommationRepository.findReportData(abonnementId, debut, fin);
    }

    @Override
    public List<LigneAbonnementReportProjection> getReportData(Long abonnementId) {
        return ligneAbonnementRepository.findReportData(abonnementId);
    }

    @Override
    public List<LigneAbonnementReportProjection> getReportData(List<Long> abonnementIds) {
        return ligneAbonnementRepository.findReportData(abonnementIds);
    }

    @Override
    public List<PaiementReportProjection> getReportData(Long abonnementId, Instant debut, Instant fin) {
        return paiementRepository.findReportData(abonnementId, debut, fin);
    }

    @Override
    public List<PaiementReportProjection> getReportData(List<Long> abonnementId, Instant debut, Instant fin) {
        return paiementRepository.findReportData(abonnementId, debut, fin);
    }


}