package com.boulangerie.comptabilite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalMouvementDto{

    private Instant date;

    private String libelle;

    private BigDecimal montant;

    private String sens;
}