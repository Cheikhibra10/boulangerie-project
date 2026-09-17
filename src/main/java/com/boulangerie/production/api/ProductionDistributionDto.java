package com.boulangerie.production.api;

import com.boulangerie.production.model.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ProductionDistributionDto {

    private CanalDistribution canal;

    private EtatPain etatPain;

    private Long livreurId;

    private String livreurNom;

    private Long abonnementId;

    private BigDecimal quantite =
            BigDecimal.ZERO;

    private BigDecimal quantiteConsommee =
            BigDecimal.ZERO;
}