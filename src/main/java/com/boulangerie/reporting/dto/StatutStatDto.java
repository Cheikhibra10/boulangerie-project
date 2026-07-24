package com.boulangerie.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.*;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatutStatDto {

    private String statut;

    private Long nombre;

    private BigDecimal montant;
}