package com.boulangerie.comptabilite.dto;

import lombok.Builder;
import lombok.Value;

import java.math.BigDecimal;

@Value
@Builder
public class ComptabiliteStatistiquesDto {
    long totalPeriodes;
    long periodesOuvertes;
    long periodesFermees;
    long periodesCloturees;
    long totalResultats;
    BigDecimal caTotal;
    BigDecimal beneficeTotal;
    BigDecimal beneficeMoyen;
    PeriodeDto dernierePeriodeCloturee;
    PeriodeDto periodeEnCours;
}