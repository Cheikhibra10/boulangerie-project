package com.boulangerie.stocks.event;

import com.boulangerie.shared.model.Notifiable;

import java.math.BigDecimal;

public record StockSeuilAtteintEvent(
        String typeArticle,       // "INGREDIENT" ou "PRODUIT"
        Long articleId,
        String articleLibelle,
        BigDecimal quantiteActuelle,
        BigDecimal seuilAlerte,
        String libelle
) implements Notifiable {

    public BigDecimal montant() {
        return null;
    }
}