package com.boulangerie.abonnements.internal;

import com.boulangerie.abonnements.api.AbonnementApi;
import com.boulangerie.abonnements.model.Abonnement;
import com.boulangerie.abonnements.repository.AbonnementRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AbonnementApiImpl implements AbonnementApi {

    private final AbonnementRepository abonnementRepository;

    @Override
    public Long findLivreurId(Long abonnementId) {
        return abonnementRepository.findById(abonnementId)
                .orElseThrow(() -> new EntityNotFoundException("Abonnement " + abonnementId))
                .getLivreurId();
    }

    @Override
    public List<Long> findIdsByLivreurId(Long livreurId) {
        return abonnementRepository.findByLivreurId(livreurId).stream()
                .map(Abonnement::getId)
                .toList();
    }
}