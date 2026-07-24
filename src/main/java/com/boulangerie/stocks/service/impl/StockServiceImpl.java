package com.boulangerie.stocks.service.impl;

import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.service.IngredientService;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.shared.dto.ConsommationIngredient;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.administration.security.CurrentUserService;
import com.boulangerie.shared.utils.PageUtils;
import com.boulangerie.stocks.dto.*;
import com.boulangerie.stocks.exception.StockInitialAlreadyExistsException;
import com.boulangerie.stocks.exception.StockInsuffisantException;
import com.boulangerie.stocks.internal.CmpCalculator;
import com.boulangerie.stocks.internal.StockAlerteService;
import com.boulangerie.stocks.mapper.*;
import com.boulangerie.stocks.model.*;
import com.boulangerie.stocks.repository.*;
import com.boulangerie.stocks.service.MouvementStockService;
import com.boulangerie.stocks.service.StockService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class StockServiceImpl implements StockService {

    private final StockIngredientRepository stockIngredientRepository;
    private final StockProduitRepository stockProduitRepository;
    private final StockInitialRepository stockInitialRepository;
    private final StockIngredientSnapshotRepository snapshotRepository;

    private final StockIngredientMapper stockIngredientMapper;
    private final StockProduitMapper stockProduitMapper;
    private final StockInitialMapper stockInitialMapper;
    private final StockIngredientSnapshotMapper snapshotMapper;

    private final IngredientService ingredientService;
    private final ProduitService produitService;
    private final MouvementStockService mouvementService;
    private final CmpCalculator cmpCalculator;
    private final StockAlerteService alerteService;
    private final CurrentUserService currentUserService;


    @Override
    @Transactional(readOnly = true)
    public StockDetailDto getStockDetail(Long ingredientId, int page, int size) {
        Ingredient ingredient = ingredientService.findIngredientOrThrow(ingredientId);
        StockIngredient stock = stockIngredientRepository.findByIngredient(ingredient)
                .orElseThrow(() -> new EntityNotFoundException("Stock pour cet ingrédient est introuvable" + ingredient.getLibelle()));
        // Utilisation de Specification via le service de mouvement
        PageResponse<MouvementStockDto> mouvements = mouvementService.rechercher(
                ingredientId, null, null, null, null, page, size
        );
        boolean alerte = stock.getQuantite().compareTo(stock.getSeuilAlerte()) < 0;
        return StockDetailDto.builder()
                .stockActuel(stockIngredientMapper.toDto(stock))
                .mouvements(mouvements.getContent())
                .totalElements(mouvements.getTotalElements())
                .page(page)
                .size(size)
                .alerte(alerte)
                .build();
    }

    @Transactional
    @Override
    public StockInitialDto creerStockInitial(Long periodeId, Long ingredientId, BigDecimal quantite, BigDecimal valeur) {
        Ingredient ingredient = ingredientService.findIngredientOrThrow(ingredientId);
        if(stockInitialRepository.existsByPeriodeIdAndIngredient(periodeId, ingredient)){
            throw new StockInitialAlreadyExistsException(periodeId, ingredientId
            );
        }
        BigDecimal cmp = quantite.compareTo(BigDecimal.ZERO) > 0 ? valeur.divide(quantite, 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;
        StockInitial stockInitial = new StockInitial()
                        .setPeriodeId(periodeId)
                        .setIngredient(ingredient)
                        .setQuantite(quantite)
                        .setValeur(valeur)
                        .setCoutMoyenPondere(cmp);
        stockInitialRepository.save(stockInitial);
        StockIngredient stock = stockIngredientRepository.findByIngredient(ingredient)
                        .orElse(new StockIngredient().setIngredient(ingredient));
        stock.setQuantite(quantite).setValeurTotale(valeur);
        stockIngredientRepository.save(stock);
        return stockInitialMapper.toDto(stockInitial);
    }

    @Override
    @Transactional
    public MouvementStockDto enregistrerMouvement(
            Long ingredientId,
            TypeMouvementStock type,
            BigDecimal quantite,
            String motif
    ) {
        verifierTypeMouvementManuel(type);
        verifierMotif(type, motif);
        MouvementStockDto mouvement = appliquerMouvementStock(
                ingredientId,
                type,
                quantite,
                motif,
                null,
                null,
                null
        );
        log.info("Mouvement manuel {} enregistré pour l'ingrédient {} par {}",
                type,
                ingredientId,
                currentUserService.getCurrentUser().getNom()
        );
        return mouvement;
    }

    @Transactional(readOnly = true)
    @Override
    public void verifierDisponibiliteIngredients(List<ConsommationIngredient> consommations) {
        for (ConsommationIngredient consommation : consommations) {

            StockIngredient stock = chargerStock(consommation.ingredientId());

            stock.verifierDisponibilite(consommation.quantite());
        }
    }

    @Override
    public StockIngredientDto modifierSeuilAlerte(Long stockId, SeuilAlerteRequest request) {
       StockIngredient stock= stockIngredientRepository.findById(stockId)
               .orElseThrow(() -> new EntityNotFoundException("Stock introuvable" + stockId));
        stock.modifierSeuilAlerte(request.getSeuilAlerte());
        stock = stockIngredientRepository.save(stock);
        log.info(
                "Seuil d'alerte du stock {} modifié à {} par {}",
                stockId,
                request.getSeuilAlerte(),
                currentUserService.getCurrentUser().getNom()
        );
        return stockIngredientMapper.toDto(stock);
    }

    @Override
    public ConsommationResultDto consommerIngredients(Long lotId, List<ConsommationIngredient> consommations) {
        List<MouvementStockDto> mouvements = new ArrayList<>();
        List<String> alertes = new ArrayList<>();
        for (ConsommationIngredient consommation : consommations) {
            consommerIngredient(lotId, consommation, mouvements, alertes);
        }
        return new ConsommationResultDto(lotId, mouvements, alertes);
    }

    private void consommerIngredient(
            Long lotId,
            ConsommationIngredient consommation,
            List<MouvementStockDto> mouvements,
            List<String> alertes
    ){
        StockIngredient stock = chargerStock(consommation.ingredientId());
        mouvements.add(appliquerMouvementStock(
                        consommation.ingredientId(),
                        TypeMouvementStock.CONSOMMATION_PRODUCTION,
                        consommation.quantite(),
                        "Consommation production lot #" + lotId,
                        lotId,
                        null,
                        alertes
                ));
        verifierSeuil(stock, alertes);
    }

    private void verifierTypeMouvementManuel(TypeMouvementStock type) {

        if (type == TypeMouvementStock.RECEPTION_ACHAT) {
            throw new BadRequestException("Une entrée de stock doit provenir d'une réception d'achat.");
        }
    }

    private BigDecimal calculerMontant(
            BigDecimal quantite,
            StockIngredient stock
    ) {
        return quantite.multiply(cmpCalculator.calculerCMP(stock)
        );
    }

    private MouvementStockDto appliquerMouvementStock(
            Long ingredientId,
            TypeMouvementStock type,
            BigDecimal quantite,
            String motif,
            Long lotProductionId,
            Long ligneAchatId,
            List<String> alertes
    ) {

        StockIngredient stock = chargerStock(ingredientId);
        stock.verifierMouvement(type, quantite);
        BigDecimal montant = calculerMontant(quantite, stock);
        MouvementStockDto mouvement = mouvementService.creerMouvement(
                type,
                ingredientId,
                quantite,
                montant,
                motif,
                lotProductionId,
                ligneAchatId
        );

        stock.appliquerMouvement(type, quantite, montant);

        verifierSeuil(stock, alertes);

        stockIngredientRepository.save(stock);

        return mouvement;
    }

    private void verifierMotif(TypeMouvementStock type, String motif) {
        if (type != TypeMouvementStock.PERTE) {
            return;
        }
        if (motif == null || motif.isBlank()) {
            throw new BadRequestException(
                    "Un motif est obligatoire pour enregistrer une perte."
            );
        }

        if (motif.trim().length() < 10) {
            throw new BadRequestException("Le motif d'une perte doit contenir au moins 10 caractères.");
        }
    }

    private void verifierSeuil(StockIngredient stock, List<String> alertes) {
        if (!stock.estSousSeuil()) {
            return;
        }
        String message = "Stock bas : " + stock.getIngredient().getLibelle();
        if (alertes != null) {
            alertes.add(message);
        }
        alerteService.alerterStockBas(stock.getIngredient(), stock);
    }

    private StockIngredient chargerStock(Long ingredientId) {
        return stockIngredientRepository.findByIngredientId(ingredientId)
                .orElseThrow(() -> new EntityNotFoundException("Stock pour cet ingredient introuvable" +ingredientId));
    }

    @Transactional
    @Override
    public void entrerStock(Long ingredientId, BigDecimal quantite, BigDecimal prixUnitaire, Long ligneAchatId) {
        Ingredient ingredient = ingredientService.findIngredientOrThrow(ingredientId);
        StockIngredient stock = stockIngredientRepository.findByIngredient(ingredient)
                .orElse(new StockIngredient().setIngredient(ingredient));
        BigDecimal montant = quantite.multiply(prixUnitaire);
        // Créer le mouvement d'entrée
        mouvementService.creerMouvement(TypeMouvementStock.RECEPTION_ACHAT, ingredientId, quantite, montant, "Entrée stock via achat ligne #" + ligneAchatId, null, ligneAchatId);
        // Mettre à jour le stock
        stock.ajouter(quantite, montant);
        stockIngredientRepository.save(stock);
        log.info("Entrée stock de {} unités de {} (achat ligne {}) par {}",
                quantite, ingredient.getLibelle(), ligneAchatId, currentUserService.getCurrentUser().getNom());
    }

    /**
     * Batch entry - For better performance with multiple lines
     */
    @Transactional
    public void entrerStock(List<StockEntry> entries) {
        if (entries.isEmpty()) {
            return;
        }
        // 1. Extract ingredient ids
        List<Long> ingredientIds = entries.stream()
                .map(StockEntry::ingredientId)
                .distinct()
                .toList();
        // 2. Load all ingredients
        Map<Long, Ingredient> ingredientMap = ingredientService.findIngredientsByIds(ingredientIds);

        // Validate ingredients
        for (Long ingredientId : ingredientIds) {
            if (!ingredientMap.containsKey(ingredientId)) {
                throw new EntityNotFoundException(
                        "Ingrédient introuvable : " + ingredientId
                );
            }
        }
        // 3. Load existing stocks
        List<Ingredient> ingredients = new ArrayList<>(ingredientMap.values());
        Map<Long, StockIngredient> stockMap =
                stockIngredientRepository.findByIngredientIn(ingredients)
                        .stream()
                        .collect(Collectors.toMap(
                                stock -> stock.getIngredient().getId(),
                                Function.identity()
                        ));
        // 4. Prepare batch operations
        Map<Long, StockIngredient> stocksToSave = new HashMap<>();
        List<MouvementStock> mouvementsToSave = new ArrayList<>();

        for (StockEntry entry : entries) {
            Ingredient ingredient = ingredientMap.get(entry.ingredientId());
            BigDecimal montant = entry.quantite().multiply(entry.prixUnitaire());

            StockIngredient stock = stockMap.get(entry.ingredientId());

            if (stock == null) {
                stock = StockIngredient.creer(ingredient);
                stockMap.put(entry.ingredientId(), stock);
            }

            stock.ajouter(entry.quantite(), montant);

            stocksToSave.put(entry.ingredientId(), stock);
            mouvementsToSave.add(
                    MouvementStock.creerReceptionAchat(
                            ingredient,
                            entry.quantite(),
                            montant,
                            entry.ligneAchatId(),
                            entry.reference()
                    )
            );
        }
        // 5. Persist
        stockIngredientRepository.saveAll(stocksToSave.values());
        mouvementService.creerMouvements(mouvementsToSave);

        log.info(
                "Entrée de stock : {} ligne(s) traitée(s) par {}",
                entries.size(),
                currentUserService.getCurrentUser().getNom()
        );
    }


    @Override
    @Transactional(readOnly = true)
    public StockProduitDto getStockProduit(Long produitId) {
        return stockProduitRepository.findByProduitId(produitId)
                .map(stockProduitMapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Stock de ce produit introuvable" +produitId));
    }

    private Produit getProduit(Long produitId){
        return produitService.findProduitOrThrow(produitId);
    }

    @Override
    @Transactional
    public void augmenterStockProduit(Long produitId, BigDecimal quantite) {
        StockProduit stock = chargerOuCreerStockProduit(produitId);
        stock.ajouter(quantite);
        stockProduitRepository.save(stock);

        log.info(  "Stock produit {} augmenté de {}", produitId, quantite);
    }

    @Override
    @Transactional
    public void diminuerStockProduit(Long produitId, BigDecimal quantite) {
        StockProduit stock = chargerStockProduit(produitId);
        stock.retirer(quantite);
        verifierSeuilProduit(stock);
        stockProduitRepository.save(stock);
        log.info(  "Stock produit {} diminué de {}", produitId, quantite);
    }

    private void verifierSeuilProduit(StockProduit stock) {
        if(!stock.estSousSeuil()){
            return;
        }
        log.warn("Stock bas pour le produit {}", stock.getProduit().getLibelle());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean verifierStockProduitSuffisant(Long produitId, BigDecimal quantite) {
        return stockProduitRepository.findByProduitId(produitId)
                .map(stock -> stock.getQuantite().compareTo(quantite) >= 0)
                .orElse(false);
    }

    public void verifierStockSuffisant(Long produitId, BigDecimal quantite) {

        if (!verifierStockProduitSuffisant(produitId, quantite)) {
            throw new StockInsuffisantException(produitId);
        }
    }

    private StockProduit chargerStockProduit(Long produitId) {

        return stockProduitRepository.findByProduitId(produitId)
                .orElseThrow(() ->
                        new EntityNotFoundException("Stock produit introuvable : " + produitId));
    }

    private StockProduit chargerOuCreerStockProduit(Long produitId) {

        Produit produit = getProduit(produitId);

        return stockProduitRepository.findByProduit(produit)
                .orElseGet(() -> new StockProduit().setProduit(produit));
    }

    public void decrementerStock(List<StockMovement> lignes){
        lignes.forEach(ligne ->
                diminuerStockProduit(ligne.produitId(), ligne.quantite()
                )
        );
    }
    public void incrementerStock(List<StockMovement> lignes){
        lignes.forEach(ligne ->
                augmenterStockProduit(ligne.produitId(), ligne.quantite()
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<StockIngredientSnapshotDto> getSnapshots(Long periodeId, int page, int size) {
        Page<StockIngredientSnapshot> snapshots = snapshotRepository.findByPeriodeId(periodeId, PageRequest.of(page, size));
        return PageUtils.toPageResponse(snapshots.map(snapshotMapper::toDto));
    }
}