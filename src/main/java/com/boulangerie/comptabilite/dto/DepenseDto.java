package com.boulangerie.comptabilite.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DepenseDto extends AbstractAuditingDto {

    private Long id;

    private Long categorieId;

    private String categorie;

    private String libelle;

    private BigDecimal montant;

    private Instant date;

    private Long periodeId;
}