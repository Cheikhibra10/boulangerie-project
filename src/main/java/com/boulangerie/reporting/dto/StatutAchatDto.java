// reporting/api/dto/StatutAchatDto.java
package com.boulangerie.reporting.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatutAchatDto {
    private String statut;
    private Long nombre;
    private BigDecimal montantTotal;
}