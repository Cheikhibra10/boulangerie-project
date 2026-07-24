// production/dto/RepartitionDto.java
package com.boulangerie.production.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistribuerProductionRequestDto extends AbstractAuditingDto {


    @NotNull(message = "La liste des destinations est obligatoire")
    private List<DestinationRequestDto> destinations;

}