// stocks/dto/StockDetailDto.java
package com.boulangerie.stocks.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockDetailDto extends AbstractAuditingDto {
    private StockIngredientDto stockActuel;
    private List<MouvementStockDto> mouvements;
    private Long totalElements;
    private int page;
    private int size;
    private Boolean alerte;
}