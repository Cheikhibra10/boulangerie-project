package com.boulangerie.administration.dto;

import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
public class ProduitUpdateDto {

    @Size(max = 100, message = "Le libelle ne doit pas dépasser 100 caractères")
    private String libelle;

    @Positive(message = "Le prix détail doit être positif")
    private BigDecimal prixDetail;

    @Positive(message = "Le prix gros doit être positif")
    private BigDecimal prixGros;

    @Positive(message = "Le prix livreur doit être positif")
    private BigDecimal prixLivreur;

    private Long categorieId;

    private Boolean actif;
}