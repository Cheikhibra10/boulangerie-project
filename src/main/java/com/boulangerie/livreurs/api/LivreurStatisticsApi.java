package com.boulangerie.livreurs.api;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface LivreurStatisticsApi {

    BigDecimal calculerCAVentesLivreurs(LocalDate debut, LocalDate fin);

    BigDecimal calculerReliquatLivreurs();
}