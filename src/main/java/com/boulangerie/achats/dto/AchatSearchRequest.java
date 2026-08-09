package com.boulangerie.achats.dto;

import com.boulangerie.achats.model.StatutAchat;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class AchatSearchRequest {

    private Long fournisseurId;

    private StatutAchat statut;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateDebut;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dateFin;

    private BigDecimal montantMin;

    private BigDecimal montantMax;

    private Boolean estPaye;

    private Boolean estAnnule;

    private Boolean estRecu;
}