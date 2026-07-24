package com.boulangerie.stocks.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockIngredientSnapshotDto extends AbstractAuditingDto {
    private Long id;
    private Long ingredientId;
    private String ingredientLibelle;
    private BigDecimal quantiteSolde;
    private BigDecimal valeurSolde;
}