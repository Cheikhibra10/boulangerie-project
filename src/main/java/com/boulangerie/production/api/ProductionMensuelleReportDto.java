package com.boulangerie.production.api;

import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ProductionMensuelleReportDto {

    private YearMonth periode;

    private List<LivreurProductionMensuelleDto> livreurs = new ArrayList<>();

    private List<ProductionMensuelleLigneDto> productions = new ArrayList<>();

    private ProductionDistributionMensuelleDto boutique = new ProductionDistributionMensuelleDto();

    private ProductionDistributionMensuelleDto rations = new ProductionDistributionMensuelleDto();

    private ProductionDistributionMensuelleDto aumone = new ProductionDistributionMensuelleDto();

    private ProductionDistributionMensuelleDto painGatePainFrais = new ProductionDistributionMensuelleDto();
    private Map<LocalDate, BigDecimal> sacsFarineParJour = new LinkedHashMap<>();
    private Map<LocalDate, BigDecimal> levureParJour = new LinkedHashMap<>();

    private Map<LocalDate, BigDecimal> ameliorantParJour = new LinkedHashMap<>();
    private Map<LocalDate, BigDecimal> quantitesPrevuesParJour = new LinkedHashMap<>();
    private BigDecimal quantitePrevueTotale = BigDecimal.ZERO;

    private BigDecimal quantiteRealiseeTotale = BigDecimal.ZERO;

    private BigDecimal sacsFarineTotal = BigDecimal.ZERO;
}