package com.boulangerie.administration.dto;

public record FournisseurFilter(
        String nom,
        String telephone,
        Boolean actif
) {}