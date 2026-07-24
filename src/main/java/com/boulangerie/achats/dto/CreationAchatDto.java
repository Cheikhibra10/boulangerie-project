package com.boulangerie.achats.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreationAchatDto extends AbstractAuditingDto {
    @NotNull(message = "Le fournisseur est obligatoire")
    private Long fournisseurId;

    @NotEmpty(message = "L'achat doit contenir au moins une ligne")
    private List<LigneAchatRequestDto> lignes;
}