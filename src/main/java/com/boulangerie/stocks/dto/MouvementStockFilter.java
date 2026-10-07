package com.boulangerie.stocks.dto;

import com.boulangerie.stocks.model.StatutMouvement;
import com.boulangerie.stocks.model.TypeMouvementStock;

import java.time.LocalDate;

public record MouvementStockFilter(
        TypeMouvementStock type,
        StatutMouvement statut,
        String ingredientLibelle,
        String motif,
        LocalDate dateDebut,
        LocalDate dateFin
) {}