package com.boulangerie.abonnements.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Accessors(chain = true)
public class ImportConsommationCommand {

    private Long abonnementId;

    private Long clientId;

    private Long ligneId;

    private LocalDate date;

    private BigDecimal quantite;
}