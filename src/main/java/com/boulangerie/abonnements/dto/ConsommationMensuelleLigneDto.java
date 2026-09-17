package com.boulangerie.abonnements.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
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

    private BigDecimal quantiteTotale = BigDecimal.ZERO;

    private BigDecimal montant = BigDecimal.ZERO;

    private BigDecimal montantPaye = BigDecimal.ZERO;

    private BigDecimal reliquat = BigDecimal.ZERO;

    public void initialiserConsommations(YearMonth periode) {
        consommations.clear();
        for (int jour=1; jour<=periode.lengthOfMonth(); jour++ ) {
            consommations.put(jour, BigDecimal.ZERO);
        }
    }

    public void ajouterConsommation(LocalDate date, BigDecimal quantite) {
        consommations.put(
                date.getDayOfMonth(),
                quantite
        );
    }
}