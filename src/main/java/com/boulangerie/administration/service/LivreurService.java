// administration/service/LivreurService.java
package com.boulangerie.administration.service;

import com.boulangerie.administration.dto.LivreurDto;
import com.boulangerie.administration.model.Livreur;
import com.boulangerie.shared.service.DefaultService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface LivreurService extends DefaultService<Livreur, LivreurDto> {

    /**
     * Liste tous les livreurs actifs (pour sélection)
     */
    List<LivreurDto> findAllActive();

    /**
     * Vérifie si un livreur est actif (RG03)
     */
    boolean isActive(Long livreurId);

    Long findLivreurOrThrow(Long id);
}