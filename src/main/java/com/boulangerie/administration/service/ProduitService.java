package com.boulangerie.administration.service;

import com.boulangerie.administration.dto.ProduitDto;
import com.boulangerie.administration.dto.ProduitUpdateDto;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.model.TypeProduit;
import com.boulangerie.shared.dto.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

public interface ProduitService{
    ProduitDto create(ProduitDto dto, MultipartFile image);

    ProduitDto update(Long id, ProduitDto dto, MultipartFile image);

    ProduitDto patch(Long id, ProduitUpdateDto dto, MultipartFile image);
    ProduitDto delete(Long id);

   ProduitDto archive(Long id);

    // Méthodes spécifiques si nécessaire
    Set<Long> findIdsByType(TypeProduit type);


    Produit findProduitOrThrow(Long id);
    ProduitDto getById(Long id);

    Long findProduitIdOrThrow(Long id);

    Long getIdByIdLibelle(String libelle);

    PageResponse<ProduitDto> findAll(int page, int size);
}