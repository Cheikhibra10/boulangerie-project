// production/dto/DestinationRequestDto.java
package com.boulangerie.production.dto;

import com.boulangerie.production.model.CanalDistribution;
import com.boulangerie.production.model.EtatPain;
import com.boulangerie.shared.dto.AbstractAuditingDto;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.*;

import java.math.BigDecimal;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DestinationRequestDto extends AbstractAuditingDto {

    @NotNull(message = "Le canal est obligatoire")
    private CanalDistribution canal;
    private EtatPain etatPain;
    @NotNull(message = "La quantité est obligatoire")
    @Positive(message = "La quantité doit être positive")
    private BigDecimal quantite;

    @NotNull(message = "Le prix unitaire est obligatoire")
    @Positive(message = "Le prix unitaire doit être positif")
    private BigDecimal prixUnitaire;

    private Long livreurId;
    private Long abonnementId;
}