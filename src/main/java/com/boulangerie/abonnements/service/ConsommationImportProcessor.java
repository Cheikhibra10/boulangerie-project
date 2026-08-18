package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.dto.ConsommationDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class ConsommationImportProcessor {

    private final AbonnementService abonnementService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void importer(
            Long ligneId,
            LocalDate date,
            BigDecimal quantite
    ) {

        ConsommationDto dto = new ConsommationDto()
                        .setDate(date)
                        .setQuantite(quantite);

        abonnementService.enregistrerConsommation(
                ligneId,
                dto
        );
    }
}