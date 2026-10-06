// caisse/dto/MouvementCaisseFilter.java
package com.boulangerie.comptabilite.dto;

import com.boulangerie.shared.model.SensMouvement;
import com.boulangerie.shared.model.TypeMouvement;

public record MouvementCaisseFilter(
        TypeMouvement typeMouvement,
        SensMouvement sens
) {}

