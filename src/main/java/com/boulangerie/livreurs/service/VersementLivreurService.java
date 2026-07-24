package com.boulangerie.livreurs.service;

import com.boulangerie.comptabilite.model.Caisse;
import com.boulangerie.shared.model.TypePaiement;
import com.boulangerie.comptabilite.service.CaisseService;
import com.boulangerie.livreurs.dto.ClotureCompteRenduDto;
import com.boulangerie.livreurs.dto.VersementLivreurDto;
import com.boulangerie.livreurs.model.CompteLivreurJournalier;
import com.boulangerie.livreurs.model.VersementLivreur;
import com.boulangerie.livreurs.repository.VersementLivreurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class VersementLivreurService {

    private final VersementLivreurRepository repository;

    public VersementLivreur creerVersement(CompteLivreurJournalier journalier, ClotureCompteRenduDto dto) {

        BigDecimal montantTotal = dto.getVersements()
                        .stream()
                        .map(VersementLivreurDto::getMontant)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

        TypePaiement modePaiement = determinerModePaiement(dto);


        VersementLivreur versement = new VersementLivreur();

        versement.setCompteRendu(journalier);
        versement.setMontant(montantTotal);
        versement.setModePaiement(modePaiement);
        return repository.save(versement);
    }

    private TypePaiement determinerModePaiement(ClotureCompteRenduDto dto) {

        if (dto.getVersements().size() == 1) {

            return dto.getVersements()
                    .getFirst()
                    .getModePaiement();
        }

        return TypePaiement.MIXTE;
    }

}