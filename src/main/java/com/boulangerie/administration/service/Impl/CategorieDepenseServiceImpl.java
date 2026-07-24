// administration/service/impl/CategorieDepenseServiceImpl.java
package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.dto.CategorieDepenseDto;
import com.boulangerie.administration.mapper.CategorieDepenseMapper;
import com.boulangerie.administration.model.CategorieDepense;
import com.boulangerie.administration.repository.CategorieDepenseRepository;
import com.boulangerie.administration.service.CategorieDepenseService;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.service.impl.AbstractCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategorieDepenseServiceImpl
        extends AbstractCrudService<CategorieDepense, CategorieDepenseDto>
        implements CategorieDepenseService {
    private final CategorieDepenseRepository repository;
    public CategorieDepenseServiceImpl(CategorieDepenseRepository repository,
                                       CategorieDepenseMapper mapper) {
        super(repository, mapper, CategorieDepense.class);
        this.repository =  repository;
    }

    @Override
    public CategorieDepenseDto create(CategorieDepenseDto dto) {
        if (repository.existsByLibelle(dto.getLibelle())) {
            throw new BadRequestException("Une catégorie de dépense avec le libellé '" + dto.getLibelle() + "' existe déjà.");
        }
        return super.create(dto);
    }

    @Override
    public CategorieDepenseDto update(Long id, CategorieDepenseDto dto) {
        repository.findByLibelle(dto.getLibelle())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Une catégorie de dépense avec le libellé '" + dto.getLibelle() + "' existe déjà.");
                    }
                });
        return super.update(id, dto);
    }


    @Override
    public CategorieDepense findCategorieDepenseOrThrow(Long categorieId){
        return repository.findById(categorieId)
                .orElseThrow(() -> new EntityNotFoundException("Catégorie introuvable"));
    }
}