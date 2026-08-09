package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.dto.LivreurDto;
import com.boulangerie.administration.mapper.LivreurMapper;
import com.boulangerie.administration.model.Livreur;
import com.boulangerie.administration.repository.LivreurRepository;
import com.boulangerie.administration.service.LivreurService;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.service.impl.AbstractCrudService;
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
}