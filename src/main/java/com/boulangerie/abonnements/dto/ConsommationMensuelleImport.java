package com.boulangerie.abonnements.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.time.YearMonth;
import java.util.*;

@Getter
@Setter
@Accessors(chain = true)
public class ConsommationMensuelleImport {

    private YearMonth periode;

    private List<ConsommationMensuelleImportLigne> lignes =
            new ArrayList<>();
}