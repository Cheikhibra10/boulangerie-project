package com.boulangerie.reporting.dto;

import lombok.*;

import java.util.List;

@Data
@Builder
public class AchatStatistiquesDto {

    private List<StatutStatDto> achats;

    private List<StatutStatDto> receptions;

    private List<StatutStatDto> paiements;
}