package com.boulangerie.comptabilite.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Data
@Accessors(chain = true)
public class VersementsRapportMensuelDto {

    private YearMonth periode;

    /*
     * Un seul jour par LocalDate présent dans le mois — les jours
     * sans aucune activité (ni versement ni frais) ne sont pas
     * inclus, l'exporteur n'a donc pas besoin de gérer des blocs
     * vides.
     */
    private List<VersementsJourDto> jours = new ArrayList<>();
}