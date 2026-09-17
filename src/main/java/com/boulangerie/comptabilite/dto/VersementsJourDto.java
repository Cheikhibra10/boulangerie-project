package com.boulangerie.comptabilite.dto;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Les lignes VERSEMENTS et FRAIS d'une seule journée.
 *
 * Contrairement au rapport de production (colonnes fixes par
 * jour), ce rapport est structuré en blocs séquentiels de hauteur
 * VARIABLE : chaque jour peut avoir un nombre différent de lignes
 * de versements et de frais. Cette classe ne porte donc aucune
 * notion de position Excel — c'est à l'exporteur de calculer les
 * lignes réelles à l'écriture, à partir de la taille de ces
 * listes.
 */
@Data
@Accessors(chain = true)
public class VersementsJourDto {

    private LocalDate date;

    private List<LigneMontantDto> versements = new ArrayList<>();

    private List<LigneMontantDto> frais = new ArrayList<>();
}