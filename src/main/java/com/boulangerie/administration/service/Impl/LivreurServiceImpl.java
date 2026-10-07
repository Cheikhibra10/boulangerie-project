package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.dto.LivreurDto;
import com.boulangerie.administration.dto.LivreurFilter;
import com.boulangerie.administration.mapper.LivreurMapper;
import com.boulangerie.administration.model.Livreur;
import com.boulangerie.administration.repository.LivreurRepository;
import com.boulangerie.administration.service.LivreurService;
import com.boulangerie.administration.specification.LivreurSpecifications;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.service.impl.AbstractCrudService;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class LivreurServiceImpl
        extends AbstractCrudService<Livreur, LivreurDto>
        implements LivreurService {

    private final LivreurRepository repository;
    public LivreurServiceImpl(LivreurRepository repository,
                              LivreurMapper mapper) {
        super(repository, mapper, Livreur.class);
        this.repository = repository;
    }

    @Override
    public List<LivreurDto> findAllActive() {
        return repository.findByActifTrue().stream()
                .map(mapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isActive(Long livreurId) {
        return repository.findByIdAndActifTrue(livreurId).isPresent();
    }

    @Override
    public LivreurDto archive(Long id) {
        Livreur livreur = getEntityById(id);
        livreur.setActif(false);
        return mapper.toDto(repository.save(livreur));
    }

    @Override
    public LivreurDto restore(Long id) {
        Livreur livreur = getEntityById(id);
        livreur.setActif(true);
        return mapper.toDto(repository.save(livreur));
    }

    @Override
    public Long findLivreurOrThrow(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Livreur introuvable : " + id);
        }
        return id;
    }

    @Override
    public Livreur findLivreurById(Long id) {
        return repository.findById(id).orElseThrow(() -> new EntityNotFoundException("Livreur introuvable : " + id));
    }

    @Transactional(readOnly = true)
    public Page<LivreurDto> search(LivreurFilter filter, Pageable pageable) {
        return repository
                .findAll(LivreurSpecifications.withFilters(filter), pageable)
                .map(mapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AutocompleteItemDto> autocomplete(String q) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        Specification<Livreur> spec = Specification
                .<Livreur>where(SearchSpecifications.isActive())
                .and(SearchSpecifications.likeAny(q, "nom", "prenom", "telephone"));

        return repository
                .findAll(spec, PageRequest.of(0, 15))
                .stream()
                .map(l -> AutocompleteItemDto.of(
                        l.getId(),
                        l.getNom() + " " + l.getPrenom(),
                        l.getTelephone()
                ))
                .toList();
    }
}