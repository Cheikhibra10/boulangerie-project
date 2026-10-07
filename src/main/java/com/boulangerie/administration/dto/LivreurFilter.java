package com.boulangerie.administration.dto;

public record LivreurFilter(
        String nom,
        String prenom,
        String telephone,
        Boolean actif
) {}