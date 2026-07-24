// administration/service/ProduitService.java
package com.boulangerie.administration.service;

import com.boulangerie.administration.dto.ProduitDto;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.shared.service.DefaultService;

public interface ProduitService extends DefaultService<Produit, ProduitDto> {
    // Méthodes spécifiques si nécessaire


    Produit findProduitOrThrow(Long id);

    Long findProduitIdOrThrow(Long id);

    Long getIdByIdLibelle(String libelle);

}