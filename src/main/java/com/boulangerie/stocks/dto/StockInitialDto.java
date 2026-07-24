// stocks/dto/StockInitialDto.java
package com.boulangerie.stocks.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockInitialDto extends AbstractAuditingDto {
    private Long id;
    private Long periodeId;
    private Long ingredientId;
    private String ingredientLibelle;
    private BigDecimal quantite;
    private BigDecimal valeur;
    private BigDecimal coutMoyenPondere;
}