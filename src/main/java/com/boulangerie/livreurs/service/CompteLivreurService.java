package com.boulangerie.livreurs.service;

import com.boulangerie.administration.model.Livreur;
import com.boulangerie.livreurs.model.CompteLivreur;
import com.boulangerie.livreurs.repository.CompteLivreurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
@Transactional
public class CompteLivreurService {

    private final CompteLivreurRepository repository;

    public void mettreAJourSolde(Long livreurId, BigDecimal nouveauSolde) {

        CompteLivreur compte = repository.findByLivreurId(livreurId)
                        .orElseGet(() -> {
                            CompteLivreur compteLivreur = new CompteLivreur();
                            compteLivreur.setLivreurId(livreurId);
                            return compteLivreur;
                        });

        compte.setSoldeActuel(nouveauSolde);

        repository.save(compte);
    }

}