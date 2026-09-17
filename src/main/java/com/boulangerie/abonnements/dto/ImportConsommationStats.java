package com.boulangerie.abonnements.dto;

import lombok.*;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
public class ImportConsommationStats {

    private int lignesTraitees;

    private int consommationsCreees;

    private int consommationsModifiees;

    private int consommationsInchangees;
}