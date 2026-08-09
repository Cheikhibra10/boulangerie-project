package com.boulangerie.comptabilite.internal;

import com.boulangerie.comptabilite.api.CaisseApi;
import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.comptabilite.model.StatutCaisse;
import com.boulangerie.comptabilite.repository.CaisseRepository;
import com.boulangerie.shared.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaisseApiImpl implements CaisseApi {
    private final CaisseRepository caisseRepository;

    @Override
    public Caisse getCaisseOuverte() {
       return caisseRepository.findByStatut(StatutCaisse.OUVERTE)
               .orElseThrow(() -> new BadRequestException("Caisse ouverte introuvable"));
    }
}
