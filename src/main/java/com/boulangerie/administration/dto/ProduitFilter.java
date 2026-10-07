// ========== PRODUIT ==========
package com.boulangerie.administration.dto;

import com.boulangerie.administration.model.TypeProduit;

public record ProduitFilter(
        String libelle,
        Boolean actif,
        TypeProduit typeProduit,
        String categorieLibelle
) {}