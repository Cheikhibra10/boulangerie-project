package com.boulangerie.ventes.service;


import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.ventes.dto.LigneRetourRequestDto;
import com.boulangerie.ventes.dto.LigneVenteRequestDto;
import com.boulangerie.ventes.dto.RetourVenteRequestDto;
import com.boulangerie.ventes.model.LigneVenteBoutique;
import com.boulangerie.ventes.model.VenteBoutique;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CalculerPaiementService {
    private final ProduitService produitService;

    public BigDecimal calculerDifference(VenteBoutique vente, RetourVenteRequestDto dto) {

        BigDecimal retour = calculerMontantRetour(vente,dto.getRetours());

        BigDecimal echange = calculerMontantEchange(dto.getEchanges());

        return echange.subtract(retour);
    }


    private BigDecimal calculerMontantRetour(VenteBoutique vente, List<LigneRetourRequestDto> retours) {

        BigDecimal montant = BigDecimal.ZERO;

        for (LigneRetourRequestDto retour : retours) {

            LigneVenteBoutique ligne = vente.getLigne(retour.getProduitId());

            montant = montant.add(ligne.getPrixUnitaire().multiply(retour.getQuantite()));
        }
        return montant;
    }


    private BigDecimal calculerMontantEchange(List<LigneVenteRequestDto> echanges) {

        BigDecimal montant = BigDecimal.ZERO;

        for (LigneVenteRequestDto ligne : echanges) {
            Produit produit = produitService.findProduitOrThrow(ligne.getProduitId());
            montant = montant.add(produit.getPrixDetail().multiply(ligne.getQuantite()));
        }
        return montant;
    }

}
