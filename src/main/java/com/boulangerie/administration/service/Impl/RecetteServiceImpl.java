package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.dto.RecetteDto;
import com.boulangerie.administration.mapper.RecetteMapper;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.model.Recette;
import com.boulangerie.administration.model.RecetteIngredient;
import com.boulangerie.administration.repository.IngredientRepository;
import com.boulangerie.administration.repository.ProduitRepository;
import com.boulangerie.administration.repository.RecetteRepository;
import com.boulangerie.administration.service.RecetteService;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.service.impl.AbstractCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class RecetteServiceImpl
        extends AbstractCrudService<Recette, RecetteDto>
        implements RecetteService {

    private final RecetteRepository recetteRepository;
    private final IngredientRepository ingredientRepository;
    private final ProduitRepository produitRepository;
    public RecetteServiceImpl(RecetteRepository repository,
                              RecetteMapper mapper,
                              RecetteRepository recetteRepository, IngredientRepository ingredientRepository, ProduitRepository produitRepository) {
        super(repository, mapper, Recette.class);
        this.recetteRepository = recetteRepository;
        this.ingredientRepository = ingredientRepository;
        this.produitRepository = produitRepository;
    }

    @Override
    @Transactional
    public RecetteDto create(RecetteDto dto) {

        if(recetteRepository.existsByProduitIdAndVersion(dto.getProduitId(), dto.getVersion())) {
            throw new BadRequestException("Une recette avec ce produit et cette version existe déjà.");
        }

        Produit produit = produitRepository.findById(dto.getProduitId())
                .orElseThrow(() -> new BadRequestException("Produit introuvable : " + dto.getProduitId()));
        Recette recette = mapper.toEntity(dto);
        recette.setProduit(produit);
        dto.getIngredients().forEach(item -> {
                    Ingredient ingredient = ingredientRepository.findById(item.getIngredientId())
                                    .orElseThrow(() -> new BadRequestException("Ingredient introuvable : " + item.getIngredientId()));
                    RecetteIngredient recetteIngredient = new RecetteIngredient()
                                    .setIngredient(ingredient)
                                    .setQuantite(item.getQuantite());
                    recette.addIngredient(recetteIngredient);
                });
        if(!recette.contientFarine()){
            throw new BadRequestException("La recette doit contenir au moins ingrédient farine.");
        }
        return mapper.toDto(recetteRepository.save(recette)
        );
    }

    @Override
    public RecetteDto update(Long id, RecetteDto dto) {
        recetteRepository.findByProduitIdAndVersion(dto.getProduitId(), dto.getVersion())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Une recette avec le produit et la version " + dto.getVersion() + " existe déjà.");
                    }
                });
        // Si la recette est activée, désactiver les autres recettes du même produit
        if (dto.getActif()) {
            recetteRepository.findActiveByProduitId(dto.getProduitId())
                    .ifPresent(active -> {
                        if (!active.getId().equals(id)) {
                            desactiverRecette(active);
                        }
                    });
        }
        return super.update(id, dto);
    }

    @Override
    public RecetteDto setActive(Long recetteId, boolean actif) {
        Recette recette = getEntityById(recetteId);
        recette.setActif(actif);

        // Si on active, désactiver les autres recettes du même produit
        if (actif) {
            recetteRepository.findActiveByProduitId(recette.getProduit().getId())
                    .ifPresent(active -> {
                        if (!active.getId().equals(recetteId)) {
                            desactiverRecette(active);
                        }
                    });
        }

        return mapper.toDto(repository.save(recette));
    }

    @Override
    public Recette findActiveByProduitIdOrThrow(Long produitId) {
        return recetteRepository.findActiveByProduitIdWithIngredients(produitId)
                .orElseThrow(() -> new EntityNotFoundException("Recette active pour le produit introuvable" +produitId));
    }

    @Override
    public RecetteDto getActiveByProduitId(Long produitId) {
        return recetteRepository.findActiveByProduitIdWithIngredients(produitId)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Recette active pour le produit " + produitId));
    }

    @Override
    public boolean hasActiveRecette(Long produitId) {
        return recetteRepository.existsByProduitIdAndActifTrue(produitId);
    }

    @Override
    public RecetteDto archive(Long id) {
        Recette recette = getEntityById(id);
        recette.setActif(false);
        return mapper.toDto(repository.save(recette));
    }

    @Override
    public RecetteDto restore(Long id) {
        Recette recette = getEntityById(id);
        recette.setActif(true);
        return mapper.toDto(repository.save(recette));
    }

    private void desactiverRecette(Recette recette) {
        recette.setActif(false);
        recetteRepository.save(recette);
    }

    @Override
    public RecetteDto delete(Long id) {
        // Vérifier que la recette n'est pas active
        Recette recette = getEntityById(id);
        if (recette.getActif()) {
            throw new BadRequestException("Impossible de supprimer une recette active. Désactivez-la d'abord.");
        }
        return super.delete(id);
    }

    @Transactional(readOnly = true)
    @Override
    public Recette findByIdOrThrow(Long recetteId) {

        return recetteRepository
                .findByIdWithIngredients(recetteId)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Recette introuvable : " + recetteId
                        ));
    }
}