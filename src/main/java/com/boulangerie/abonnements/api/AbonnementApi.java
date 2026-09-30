package com.boulangerie.abonnements.api;

import java.util.List;

public interface AbonnementApi {
    Long findLivreurId(Long abonnementId);
    List<Long> findIdsByLivreurId(Long livreurId);
}