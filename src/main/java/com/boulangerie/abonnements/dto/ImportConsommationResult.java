package com.boulangerie.abonnements.dto;

import com.boulangerie.abonnements.model.ImportAction;

public record ImportConsommationResult(
        ImportAction action
) {
}