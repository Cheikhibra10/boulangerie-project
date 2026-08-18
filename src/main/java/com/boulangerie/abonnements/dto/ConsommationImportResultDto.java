package com.boulangerie.abonnements.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.YearMonth;
import java.util.*;

@Getter
@Setter
@Accessors(chain = true)
public class ConsommationImportResultDto {


    private int lignesTraitees;

    private int consommationsImportees;

    private int lignesIgnorees;

    private List<String> erreurs =
            new ArrayList<>();
}