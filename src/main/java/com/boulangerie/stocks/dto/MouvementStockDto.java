// stocks/dto/MouvementStockDto.java
package com.boulangerie.stocks.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import com.boulangerie.stocks.model.TypeMouvementStock;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MouvementStockDto extends AbstractAuditingDto {
    private Long id;
    private TypeMouvementStock type;
    private Long ingredientId;
    private String ingredientLibelle;
    private BigDecimal quantite;
    private BigDecimal montant;
    private Instant date;
    private String motif;
    private Long lotProductionId;
    private Long ligneAchatId;
}