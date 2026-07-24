package com.boulangerie.ventes.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.*;

@Data
public class RetourVenteRequestDto{

    @NotEmpty
    private List<LigneRetourRequestDto> retours;

    private List<LigneVenteRequestDto> echanges = new ArrayList<>();

    private String motif;
}