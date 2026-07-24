package com.boulangerie.ventes.service;

import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.ventes.dto.LigneVenteRequestDto;
import com.boulangerie.ventes.model.LigneVenteBoutique;
import com.boulangerie.ventes.model.VenteBoutique;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VenteFactory {

    private final ProduitService produitService;

    public VenteBoutique creer(
            Long caisseId,
            Utilisateur utilisateur,
            List<LigneVenteRequestDto> lignesDto
    ) {

        VenteBoutique vente = new VenteBoutique();

        vente.setDate(LocalDate.now());
        vente.setCaisseId(caisseId);
        vente.setUtilisateur(utilisateur);

        for (LigneVenteRequestDto dto : lignesDto) {

            Produit produit = produitService.findProduitOrThrow(dto.getProduitId());

            LigneVenteBoutique ligne = new LigneVenteBoutique();

            ligne.initialiser(dto.getProduitId(), produit.getLibelle(), dto.getQuantite(), produit.getPrixDetail());

            vente.ajouterLigne(ligne);
        }
        vente.finaliser();
        return vente;
    }
}