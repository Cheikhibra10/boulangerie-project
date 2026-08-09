// ventes/dto/VenteDto.java
package com.boulangerie.ventes.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import com.boulangerie.shared.model.TypePaiement;
import com.boulangerie.ventes.model.StatutVente;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VenteDto extends AbstractAuditingDto {
    private Long id;
    private String numero;
    private LocalDate date;
    @JsonFormat(pattern = "HH:mm:ss")
    private LocalTime heure;
    private String caissier;
    private StatutVente statut;
    private String motifAnnulation;
    private BigDecimal articlesVendus;
    private BigDecimal total;
    private PaiementDto paiement;
    private List<LigneVenteDto> lignes;

}