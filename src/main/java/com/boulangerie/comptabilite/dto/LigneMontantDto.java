package com.boulangerie.comptabilite.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

/**
 * Une ligne du rapport mensuel des versements/frais : un libellé
 * et un montant, sans autre structure.
 *
 * Représente aussi bien une ligne "VERSEMENTS" (ex: "Boutique",
 * "Yade", "Paiement reliquat Yade", "credit Ndoumbé") qu'une ligne
 * "FRAIS" (ex: "location yade", "salaire personnel").
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class LigneMontantDto {

    private String libelle;

    private BigDecimal montant;
}