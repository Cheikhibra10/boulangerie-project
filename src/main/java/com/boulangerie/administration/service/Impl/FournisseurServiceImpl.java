package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.dto.FournisseurDto;
import com.boulangerie.administration.dto.FournisseurFilter;
import com.boulangerie.administration.mapper.FournisseurMapper;
import com.boulangerie.administration.model.Fournisseur;
import com.boulangerie.administration.repository.FournisseurRepository;
import com.boulangerie.administration.service.FournisseurService;
import com.boulangerie.administration.specification.FournisseurSpecifications;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.repository.GenericRepository;
import com.boulangerie.shared.service.impl.AbstractCrudService;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional(readOnly = true)
    public Page<FournisseurDto> search(FournisseurFilter filter, Pageable pageable) {
        return repository
                .findAll(FournisseurSpecifications.withFilters(filter), pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AutocompleteItemDto> autocomplete(String q) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        Specification<Fournisseur> spec = Specification
                .<Fournisseur>where(SearchSpecifications.isActive())
                .and(SearchSpecifications.likeAny(q, "nom", "telephone"));

        return repository
                .findAll(spec, PageRequest.of(0, 15))
                .stream()
                .map(f -> AutocompleteItemDto.of(
                        f.getId(),
                        f.getNom(),
                        f.getTelephone()
                ))
                .toList();
    }
}