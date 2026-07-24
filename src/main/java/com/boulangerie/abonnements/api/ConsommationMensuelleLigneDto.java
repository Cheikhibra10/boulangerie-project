package com.boulangerie.abonnements.api;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
@Accessors(chain = true)
public class ConsommationMensuelleLigneDto {

    private Long ligneId;

    private Long clientId;

    private String nom;
    private String prenom;
    private BigDecimal prixUnitaire;

    private Map<Integer, BigDecimal> consommations = new LinkedHashMap<>();

    private BigDecimal quantiteTotale;

    private BigDecimal montant;

    private BigDecimal montantPaye;

    private BigDecimal reliquat;

}