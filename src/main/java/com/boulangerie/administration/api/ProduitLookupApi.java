package com.boulangerie.administration.api;

import java.util.Collection;
import java.util.List;
import java.util.Map;

// administration/api/ProduitLookupApi.java
public interface ProduitLookupApi {
    List<Long> findIdsByLibelle(String libelle);
    Map<Long, String> findLibellesByIds(Collection<Long> ids);
}