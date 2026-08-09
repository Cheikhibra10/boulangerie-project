package com.boulangerie.ventes.api;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;

public interface LigneVenteBoutiqueStatisticsApi {
    BigDecimal calculerCAVentesBoutique(LocalDate debut, LocalDate fin);
    BigDecimal calculerCAVenteRestants(LocalDate debut, LocalDate fin);
    BigDecimal calculerCAAutresProduits(LocalDate debut, LocalDate fin, Collection<Long> painIds);
}
