package com.boulangerie.livreurs.service;

import com.boulangerie.administration.service.LivreurService;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.livreurs.dto.*;
import com.boulangerie.livreurs.mapper.CommissionRegleMapper;
import com.boulangerie.livreurs.model.CommissionRegle;
import com.boulangerie.livreurs.repository.CommissionRegleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class CommissionRegleService {

    private final CommissionRegleRepository repository;

    private final LivreurService livreurService;

    private final ProduitService produitService;

    private final CommissionRegleMapper mapper;

    public CommissionRegleDto creer(CommissionRegleRequest dto) {

        livreurService.findLivreurOrThrow(dto.getLivreurId());

        produitService.findProduitIdOrThrow(dto.getProduitId());

        repository
                .findTopByLivreurIdAndProduitIdOrderByDateDebutDesc(
                        dto.getLivreurId(),
                        dto.getProduitId())
                .ifPresent(active -> {

                    if (active.getDateFin() == null) {

                        active.fermer(dto.getDateDebut().minusDays(1));

                    }

                });

        CommissionRegle regle = CommissionRegle.creer(
                        dto.getLivreurId(),
                        dto.getProduitId(),
                        dto.getMontantParUnite(),
                        dto.getDateDebut());

        repository.save(regle);
        return mapper.toDto(regle);

    }

}