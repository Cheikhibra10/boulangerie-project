package com.boulangerie.administration.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface LivreurLookupApi {
    List<Long> findIdsByNom(String nom);
    Map<Long, String> findNomsByIds(Collection<Long> ids);
}