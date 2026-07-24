// administration/service/impl/IngredientServiceImpl.java
package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.dto.IngredientDto;
import com.boulangerie.administration.mapper.IngredientMapper;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.repository.IngredientRepository;
import com.boulangerie.administration.repository.RecetteIngredientRepository;
import com.boulangerie.administration.service.IngredientService;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.repository.GenericRepository;
import com.boulangerie.shared.service.impl.AbstractCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
public class IngredientServiceImpl
        extends AbstractCrudService<Ingredient, IngredientDto>
        implements IngredientService {
    private final IngredientRepository repository;
    private final RecetteIngredientRepository recetteIngredientRepository;

    public IngredientServiceImpl(IngredientRepository repository,
                                 IngredientMapper mapper,
                                 RecetteIngredientRepository recetteIngredientRepository) {
        super(repository, mapper, Ingredient.class);
        this.repository = repository;
        this.recetteIngredientRepository = recetteIngredientRepository;
    }

    @Override
    public IngredientDto create(IngredientDto dto) {
        if (repository.existsByLibelle(dto.getLibelle())) {
            throw new BadRequestException("Un ingrédient avec le libellé '" + dto.getLibelle() + "' existe déjà.");
        }
        return super.create(dto);
    }

    @Override
    public IngredientDto update(Long id, IngredientDto dto) {
        repository.findByLibelle(dto.getLibelle())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Un ingrédient avec le libellé '" + dto.getLibelle() + "' existe déjà.");
                    }
                });
        return super.update(id, dto);
    }

    @Override
    public IngredientDto delete(Long id) {
        // Vérifier si l'ingrédient est utilisé dans une recette active
        if (recetteIngredientRepository.existsByIngredientIdInActiveRecette(id)) {
            throw new BadRequestException("Impossible de supprimer cet ingrédient car il est utilisé dans une recette active.");
        }
        return super.delete(id);
    }

    @Override
    public IngredientDto archive(Long id) {
        // Vérifier si l'ingrédient est utilisé dans une recette active
        if (recetteIngredientRepository.existsByIngredientIdInActiveRecette(id)) {
            throw new BadRequestException("Impossible d'archiver cet ingrédient car il est utilisé dans une recette active.");
        }
        Ingredient entity = getEntityById(id);
        entity.setActif(false);
        return mapper.toDto(repository.save(entity));
    }

    @Override
    public Ingredient findIngredientOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ingrédient introuvable " + id)
                );
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Ingredient> findIngredientsByIds(List<Long> ingredientIds) {

        Map<Long, Ingredient> ingredients = repository.findAllById(ingredientIds)
                .stream()
                .collect(Collectors.toMap(
                        Ingredient::getId,
                        Function.identity()
                ));

        List<Long> missingIds = ingredientIds.stream()
                .filter(id -> !ingredients.containsKey(id))
                .distinct()
                .toList();

        if (!missingIds.isEmpty()) {
            throw new EntityNotFoundException(
                    "Ingrédients introuvables : " + missingIds
            );
        }

        return ingredients;
    }
}