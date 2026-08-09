package com.boulangerie.ventes.service;

import com.boulangerie.ventes.dto.PaiementRequestDto;
import com.boulangerie.ventes.model.Paiement;
import com.boulangerie.ventes.model.VenteBoutique;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PaiementFactory {

    public Paiement creer(
            VenteBoutique vente,
            PaiementRequestDto dto
    ) {
        Paiement paiement = new Paiement();

        paiement.setMontant(vente.getTotal());
        paiement.setModePaiement(dto.getModePaiement());
        paiement.setLibelle("Vente boutique #" + vente.getId());
        return paiement;
    }
}