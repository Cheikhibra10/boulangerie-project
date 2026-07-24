package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.dto.FournisseurDto;
import com.boulangerie.administration.mapper.FournisseurMapper;
import com.boulangerie.administration.model.Fournisseur;
import com.boulangerie.administration.repository.FournisseurRepository;
import com.boulangerie.administration.service.FournisseurService;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.repository.GenericRepository;
import com.boulangerie.shared.service.impl.AbstractCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class FournisseurServiceImpl
        extends AbstractCrudService<Fournisseur, FournisseurDto>
        implements FournisseurService {
    private final FournisseurRepository repository;
    public FournisseurServiceImpl(FournisseurRepository repository,
                                  FournisseurMapper mapper) {
        super(repository, mapper, Fournisseur.class);
        this.repository = repository;
    }

    @Override
    public FournisseurDto create(FournisseurDto dto) {
        if (repository.existsByNom(dto.getNom())) {
            throw new BadRequestException("Un fournisseur avec le nom '" + dto.getNom() + "' existe déjà.");
        }
        return super.create(dto);
    }

    @Override
    public FournisseurDto update(Long id, FournisseurDto dto) {
        repository.findByNom(dto.getNom())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Un fournisseur avec le nom '" + dto.getNom() + "' existe déjà.");
                    }
                });
        return super.update(id, dto);
    }
}