// production/dto/LotProductionDto.java
package com.boulangerie.production.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LotProductionDto extends AbstractAuditingDto {
    private Long id;
    private LocalDate date;
    private Long recetteId;
    private Long produitId;
    private BigDecimal sacsFarineUtilises;
    private BigDecimal quantitePrevue;
    private BigDecimal quantiteRealisee;
    private BigDecimal ecart;
}