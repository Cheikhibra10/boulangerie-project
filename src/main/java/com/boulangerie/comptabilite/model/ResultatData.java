package com.boulangerie.comptabilite.model;

import lombok.Builder;
import lombok.*;

import java.math.BigDecimal;

@Value
@Builder
@Data
public class ResultatData {
    BigDecimal caAbonnements;
    BigDecimal caVentesLivreurs;
    BigDecimal caVentesBoutique;
    BigDecimal caVenteRestants;
    BigDecimal caAutresProduits;
    BigDecimal totalCharges;
    BigDecimal reliquatLivreurs;
    BigDecimal creditsClients;
    BigDecimal partGerant;
    BigDecimal partBoulangerie;
    
    // Calculated fields for convenience
    public BigDecimal getCaTotal() {
        return caAbonnements
                .add(caVentesLivreurs)
                .add(caVentesBoutique)
                .add(caVenteRestants)
                .add(caAutresProduits);
    }
    
    public BigDecimal getBeneficeBrut() {
        return getCaTotal().subtract(totalCharges);
    }
    
    public BigDecimal getBeneficeDistribuable() {
        return getBeneficeBrut()
                .subtract(reliquatLivreurs)
                .subtract(creditsClients);
    }
    
    public BigDecimal getBeneficeNet() {
        return partGerant.add(partBoulangerie);
    }
}