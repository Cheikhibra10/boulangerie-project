// livreurs/dto/ClotureCompteRenduDto.java
package com.boulangerie.livreurs.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClotureCompteRenduDto {

    @NotNull(message = "La liste des versements est obligatoire")
    private List<VersementLivreurDto> versements;
}