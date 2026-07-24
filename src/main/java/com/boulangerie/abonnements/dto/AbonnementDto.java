// abonnements/dto/AbonnementDto.java
package com.boulangerie.abonnements.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AbonnementDto extends AbstractAuditingDto {
    private Long id;
    private String nom;
    private String adresse;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long livreurId;
    private Boolean actif;
    private List<LigneAbonnementDto> lignes;
    private BigDecimal caTotal;
}