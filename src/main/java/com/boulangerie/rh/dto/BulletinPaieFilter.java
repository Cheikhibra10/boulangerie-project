package com.boulangerie.rh.dto;

import com.boulangerie.rh.model.StatutBulletin;

public record BulletinPaieFilter(
        String employeNom,
        Long periodeId,
        StatutBulletin statut
) {}
