package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.dto.CategorieProduitDto;
import com.boulangerie.administration.mapper.CategorieProduitMapper;
import com.boulangerie.administration.model.CategorieProduit;
import com.boulangerie.administration.repository.CategorieProduitRepository;
import com.boulangerie.administration.service.CategorieProduitService;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.repository.GenericRepository;
import com.boulangerie.shared.service.impl.AbstractCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CategorieProduitServiceImpl
        extends AbstractCrudService<CategorieProduit, CategorieProduitDto>
        implements CategorieProduitService {

    private final CategorieProduitRepository repository;
    public CategorieProduitServiceImpl(CategorieProduitRepository repository,
                                       CategorieProduitMapper mapper) {
        super(repository, mapper, CategorieProduit.class);
        this.repository = repository;
    }

    @Override
    public CategorieProduitDto create(CategorieProduitDto dto) {
        if (repository.existsByLibelle(dto.getLibelle())) {
            throw new BadRequestException("Une catégorie avec le libellé '" + dto.getLibelle() + "' existe déjà.");
        }
        return super.create(dto);
    }

    @Override
    public CategorieProduitDto update(Long id, CategorieProduitDto dto) {
        repository.findByLibelle(dto.getLibelle())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Une catégorie avec le libellé '" + dto.getLibelle() + "' existe déjà.");
                    }
                });
        return super.update(id, dto);
    }
}