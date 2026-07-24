package com.boulangerie.stocks.internal;

import com.boulangerie.stocks.api.StockManagement;
import com.boulangerie.stocks.dto.ReceptionStockLine;
import com.boulangerie.stocks.dto.RetourStockLine;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.service.IngredientService;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.stocks.dto.StockOperation;
import com.boulangerie.stocks.model.MouvementStock;
import com.boulangerie.stocks.model.StockIngredient;
import com.boulangerie.stocks.model.TypeMouvementStock;
import com.boulangerie.stocks.repository.StockIngredientRepository;
import com.boulangerie.stocks.service.MouvementStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
class StockManagementImpl implements StockManagement {
    private final CmpCalculator cmpCalculator;
    private final StockIngredientRepository stockIngredientRepository;
    private final IngredientService ingredientService;
    private final MouvementStockService mouvementService;

    @Override
    @Transactional
    public void receptionnerAchat(
            Long achatId,
            List<ReceptionStockLine> lignes
    ) {

        appliquerOperationsStock(
                lignes.stream()
                        .map(l -> new StockOperation(
                                l.ingredientId(),
                                l.quantite(),
                                l.prixUnitaire(),
                                l.ligneAchatId(),
                                l.reference(),
                                "Reception d'un ligne achat " + l.ligneAchatId()
                        ))
                        .toList(),
                TypeMouvementStock.RECEPTION_ACHAT
        );
    }

    @Override
    @Transactional
    public void retournerAchat(
            Long achatId,
            List<RetourStockLine> lignes
    ) {

        appliquerOperationsStock(
                lignes.stream()
                        .map(l -> new StockOperation(
                                l.ingredientId(),
                                l.quantite(),
                                l.prixUnitaire(),
                                l.ligneAchatId(),
                                null,
                                l.motif()
                        ))
                        .toList(),
                TypeMouvementStock.RETOUR_FOURNISSEUR
        );
    }

    private void appliquerOperationsStock(
            List<StockOperation> operations,
            TypeMouvementStock type
    ) {

        if (operations.isEmpty()) {
            return;
        }

        Map<Long, Ingredient> ingredientMap = chargerIngredients(operations);

        Map<Long, StockIngredient> stockMap = chargerStocks(ingredientMap);

        Map<Long, StockIngredient> stocksToSave = new HashMap<>();

        List<MouvementStock> mouvements = new ArrayList<>();

        for (StockOperation operation : operations) {

            Ingredient ingredient = ingredientMap.get(operation.ingredientId());

            StockIngredient stock = stockMap.computeIfAbsent(
                    operation.ingredientId(),
                    id -> StockIngredient.creer(ingredient)
            );

            mouvements.add(
                    appliquerOperation(
                            stock,
                            ingredient,
                            operation,
                            type
                    )
            );

            stocksToSave.put(
                    ingredient.getId(),
                    stock
            );
        }

        stockIngredientRepository.saveAll(stocksToSave.values());

        mouvementService.creerMouvements(mouvements);
    }

    private MouvementStock appliquerOperation(
            StockIngredient stock,
            Ingredient ingredient,
            StockOperation operation,
            TypeMouvementStock type
    ) {

        return switch (type) {

            case RECEPTION_ACHAT ->
                    receptionner(
                            stock,
                            ingredient,
                            operation
                    );

            case RETOUR_FOURNISSEUR ->
                    retourner(
                            stock,
                            ingredient,
                            operation
                    );

            default ->
                    throw new IllegalArgumentException(
                            "Type non supporté : " + type
                    );
        };
    }
    private MouvementStock receptionner(
            StockIngredient stock,
            Ingredient ingredient,
            StockOperation operation
    ) {

        BigDecimal montant = operation.quantite().multiply(operation.prixUnitaire());

        stock.ajouter(operation.quantite(), montant);

        return MouvementStock.creerReceptionAchat(
                ingredient,
                operation.quantite(),
                montant,
                operation.ligneAchatId(),
                operation.reference()
        );
    }

    private MouvementStock retourner(
            StockIngredient stock,
            Ingredient ingredient,
            StockOperation operation
    ) {

        stock.verifierDisponibilite(operation.quantite());

        BigDecimal montant = calculerMontant(operation.quantite(), stock);

        stock.retirer(operation.quantite(), montant);

        return MouvementStock.creerRetourFournisseur(
                ingredient,
                operation.quantite(),
                montant,
                operation.ligneAchatId(),
                operation.motif()
        );
    }

    private Map<Long, Ingredient> chargerIngredients(List<StockOperation> operations) {
        List<Long> ids = operations.stream()
                .map(StockOperation::ingredientId)
                .distinct()
                .toList();

        Map<Long, Ingredient> ingredients = ingredientService.findIngredientsByIds(ids);

        ids.forEach(id -> {
            if (!ingredients.containsKey(id)) {
                throw new EntityNotFoundException("Ingrédient introuvable : " + id);
            }
        });
        return ingredients;
    }

    private Map<Long, StockIngredient> chargerStocks(Map<Long, Ingredient> ingredients) {
        return stockIngredientRepository.findByIngredientIn(new ArrayList<>(ingredients.values()))
                .stream()
                .collect(Collectors.toMap(
                        stock -> stock.getIngredient().getId(),
                        Function.identity()
                ));
    }

    private BigDecimal calculerMontant(
            BigDecimal quantite,
            StockIngredient stock
    ) {
        return quantite.multiply(cmpCalculator.calculerCMP(stock)
        );
    }

}