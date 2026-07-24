// caisse/dto/JournalCaisseDto.java
package com.boulangerie.comptabilite.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JournalCaisseDto {
    private List<MouvementCaisseDto> mouvements;
    private BigDecimal totalEntrees;
    private BigDecimal totalSorties;
    private BigDecimal soldeTheorique;
    private Long totalElements;
    private int page;
    private int size;
}