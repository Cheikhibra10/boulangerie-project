package com.boulangerie.livreurs.service;

import com.boulangerie.livreurs.model.CompteLivreurJournalier;
import com.boulangerie.livreurs.repository.CompteLivreurJournalierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;


@Service
@RequiredArgsConstructor
@Transactional
public class CompteLivreurJournalierFactory {

    private final CompteLivreurJournalierRepository journalierRepository;

    public CompteLivreurJournalier create(Long livreurId, LocalDate date) {

        BigDecimal reliquatReport = getPreviousReliquat(livreurId);

        CompteLivreurJournalier journalier = new CompteLivreurJournalier();

        journalier.setLivreurId(livreurId);
        journalier.setDate(date);
        journalier.setReliquatReport(reliquatReport);

        try {
            return journalierRepository.save(journalier);
        } catch (DataIntegrityViolationException e) {
            // another transaction already created it
            return journalierRepository.findByLivreurIdAndDate(livreurId, date)
                    .orElseThrow(() -> new IllegalStateException("Impossible de créer le compte rendu"));
        }
    }

    private BigDecimal getPreviousReliquat(Long livreurId) {

        return journalierRepository
                .findTopByLivreurIdOrderByDateDesc(livreurId)
                .map(CompteLivreurJournalier::getReliquatFin)
                .orElse(BigDecimal.ZERO);
    }
}