// stocks/dto/ConsommationResultDto.java
package com.boulangerie.stocks.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsommationResultDto {

    private Long lotId;

    private List<MouvementStockDto> mouvements;

    private List<String> alertes;
}