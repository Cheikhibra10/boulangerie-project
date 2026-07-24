// stocks/dto/StockProduitDto.java
package com.boulangerie.stocks.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockProduitDto extends AbstractAuditingDto {
    private Long id;
    private Long produitId;
    private String produitLibelle;
    private BigDecimal quantite;
    private BigDecimal valeurTotale;
    private BigDecimal seuilAlerte;
    private Boolean alerte;
}