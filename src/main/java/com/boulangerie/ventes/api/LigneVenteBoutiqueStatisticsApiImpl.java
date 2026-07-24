package com.boulangerie.ventes.api;

import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.ventes.model.TypeVenteLigne;
import com.boulangerie.ventes.repository.LigneVenteBoutiqueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LigneVenteBoutiqueStatisticsApiImpl implements LigneVenteBoutiqueStatisticsApi {
    private final LigneVenteBoutiqueRepository repository;
    private final ProduitService produitService;
    @Override
    public BigDecimal calculerCAVentesBoutique(LocalDate debut, LocalDate fin) {
        return repository.sumCaBoutiqueBetweenDates(debut, fin, TypeVenteLigne.NORMALE );
    }

    @Override
    public BigDecimal calculerCAVenteRestants(LocalDate debut, LocalDate fin) {
        return repository.sumCaBoutiqueBetweenDates(debut,fin, TypeVenteLigne.RESTANT);
    }

    @Override
    public BigDecimal calculerCAAutresProduits(LocalDate debut, LocalDate fin, Collection<Long> excludedProductIds) {
        Collection<Long> excludedIds = List.of(
                produitService.getIdByIdLibelle("GP_1KG"),
                produitService.getIdByIdLibelle("PP_DEMI_KG")
        );
        return repository.sumCaAutresProduitsBetweenDates(debut, fin, excludedProductIds);
    }
}
