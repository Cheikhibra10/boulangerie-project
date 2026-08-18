package com.boulangerie.reporting.dto;

import lombok.*;
import lombok.experimental.Accessors;

import java.util.*;

@Getter
@Setter
@Accessors(chain = true)
public class ImportConsommationResultDto {

    private int lignesLues;

    private int consommationsImportees;

    private int nombreLignesIgnorees;

    private int erreurs;

    private List<String> messages = new ArrayList<>();
}