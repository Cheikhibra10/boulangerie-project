// stocks/service/impl/MouvementStockServiceImpl.java
package com.boulangerie.stocks.service.impl;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.repository.IngredientRepository;
import com.boulangerie.shared.dto.AutocompleteItemDto;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.specification.SearchSpecifications;
import com.boulangerie.shared.utils.PageUtils;
import com.boulangerie.stocks.dto.MouvementStockDto;
import com.boulangerie.stocks.dto.MouvementStockFilter;
import com.boulangerie.stocks.mapper.MouvementStockMapper;
import com.boulangerie.stocks.model.MouvementStock;
import com.boulangerie.stocks.model.StatutMouvement;
import com.boulangerie.stocks.model.TypeMouvementStock;
import com.boulangerie.stocks.repository.MouvementStockRepository;
import com.boulangerie.stocks.service.MouvementStockService;
import com.boulangerie.stocks.specification.MouvementStockSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class MouvementStockServiceImpl implements MouvementStockService {

    private final MouvementStockRepository mouvementRepository;
    private final IngredientRepository ingredientRepository;
    private final MouvementStockMapper mouvementMapper;

    @Override
    public MouvementStockDto creerMouvement(
            TypeMouvementStock type,
            Long ingredientId,
            BigDecimal quantite,
            BigDecimal montant,
            String motif,
            Long lotProductionId,
            Long ligneAchatId) {

        if (type == null) {
            throw new BadRequestException("Le type de mouvement est obligatoire");
        }

        if (quantite == null || quantite.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La quantité doit être positive");
        }

        if (montant == null || montant.compareTo(BigDecimal.ZERO) < 0) {
            throw new BadRequestException("Le montant doit être positif");
        }

        Ingredient ingredient = ingredientRepository.findById(ingredientId)
                .orElseThrow(() -> new EntityNotFoundException("Ingrédient introuvable" +ingredientId));

        MouvementStock mouvement = new MouvementStock()
                .setType(type)
                .setIngredient(ingredient)
                .setQuantite(quantite)
                .setMontant(montant)
                .setDate(Instant.now())
                .setMotif(motif)
                .setLotProductionId(lotProductionId)
                .setLigneAchatId(ligneAchatId)
                .setStatut(StatutMouvement.VALIDE);

        mouvement = mouvementRepository.save(mouvement);
        log.debug("Mouvement stock créé: {}", mouvement.getId());
        return mouvementMapper.toDto(mouvement);
    }

    // ===== NEW BATCH METHOD =====
    @Override
    public List<MouvementStockDto> creerMouvements(List<MouvementStock> mouvements) {
        if (mouvements == null || mouvements.isEmpty()) {
            return Collections.emptyList();
        }
        // 1. Validate all movements
        for (MouvementStock mouvement : mouvements) {
            validateMouvement(mouvement);
        }

        // 2. Bulk fetch ingredients
        List<Long> ingredientIds = mouvements.stream()
                .map(m -> m.getIngredient().getId())
                .distinct()
                .toList();

        Map<Long, Ingredient> ingredientMap = ingredientRepository.findAllById(ingredientIds)
                .stream()
                .collect(Collectors.toMap(Ingredient::getId, Function.identity()));

        // 3. Set ingredients and defaults
        for (MouvementStock mouvement : mouvements) {
            Ingredient ingredient = ingredientMap.get(mouvement.getIngredient().getId());
            if (ingredient == null) {
                throw new EntityNotFoundException("Ingrédient introuvable: " + mouvement.getIngredient().getId());
            }
            mouvement.setIngredient(ingredient);
            mouvement.setDate(Instant.now());
            mouvement.setStatut(StatutMouvement.VALIDE);
        }

        // 4. Batch save
        List<MouvementStock> savedMouvements = mouvementRepository.saveAll(mouvements);
        log.info("{} mouvements stock créés en batch", savedMouvements.size());

        return savedMouvements.stream()
                .map(mouvementMapper::toDto)
                .toList();
    }

    private void validateMouvement(MouvementStock mouvement) {
        if (mouvement.getQuantite() == null || mouvement.getQuantite().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BadRequestException("La quantité doit être positive");
        }
        if (mouvement.getIngredient() == null || mouvement.getIngredient().getId() == null) {
            throw new BadRequestException("L'ingrédient est obligatoire");
        }
        if (mouvement.getType() == null) {
            throw new BadRequestException("Le type de mouvement est obligatoire");
        }
    }

    @Transactional(readOnly = true)
    public Page<MouvementStockDto> search(MouvementStockFilter filter, Pageable pageable) {
        Specification<MouvementStock> spec = MouvementStockSpecifications.withFilters(filter);

        return mouvementRepository
                .findAll(spec, pageable)
                .map(mouvementMapper::toDto);
    }

    @Transactional(readOnly = true)
    public List<AutocompleteItemDto> autocomplete(String q) {
        if (q == null || q.trim().length() < 2) {
            return List.of();
        }

        Specification<MouvementStock> spec = Specification
                .<MouvementStock>where(SearchSpecifications.like("ingredient.libelle", q))
                .or(SearchSpecifications.like("motif", q));

        return mouvementRepository
                .findAll(spec, PageRequest.of(0, 15))
                .stream()
                .map(m -> AutocompleteItemDto.of(
                        m.getId(),
                        m.getIngredient() != null
                                ? m.getIngredient().getLibelle()
                                : "Mouvement #" + m.getId(),
                        m.getType() != null ? m.getType().name() : null
                ))
                .toList();
    }


    @Override
    @Transactional(readOnly = true)
    public MouvementStock getMouvementEntity(Long id) {
        return mouvementRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("MouvementStock introuvable" +id));
    }

}