// ventes/dto/InventaireResultDto.java
package com.boulangerie.ventes.dto;

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
public class InventaireResultDto {
    private List<EcartRestantDto> ecarts;
}

