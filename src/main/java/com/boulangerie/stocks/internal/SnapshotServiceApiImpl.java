package com.boulangerie.stocks.internal;

import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.utils.PageUtils;
import com.boulangerie.stocks.api.SnapshotServiceApi;
import com.boulangerie.stocks.dto.StockIngredientSnapshotDto;
import com.boulangerie.stocks.mapper.StockIngredientSnapshotMapper;
import com.boulangerie.stocks.model.*;
import com.boulangerie.stocks.repository.StockIngredientRepository;
import com.boulangerie.stocks.repository.StockIngredientSnapshotRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SnapshotServiceApiImpl implements SnapshotServiceApi {

    private final StockIngredientRepository stockRepository;
    private final StockIngredientSnapshotRepository snapshotRepository;
    private final StockIngredientSnapshotMapper snapshotMapper;

    @Transactional
    public void creerSnapshots(Long periodeId) {
        List<StockIngredient> stocks = stockRepository.findByQuantiteGreaterThan(BigDecimal.ZERO);

        if (stocks.isEmpty()) {
            log.info("Aucun stock à snapshotter pour la période {}", periodeId);
            return;
        }
        // Batch creation
        List<StockIngredientSnapshot> snapshots = stocks.stream()
                .map(stock -> new StockIngredientSnapshot()
                        .setIngredient(stock.getIngredient())
                        .setPeriodeId(periodeId)
                        .setQuantiteSolde(stock.getQuantite())
                        .setValeurSolde(stock.getValeurTotale()))
                .toList();
        // Bulk insert
        List<StockIngredientSnapshot> saved = snapshotRepository.saveAll(snapshots);
        log.info("{} snapshots créés pour la période {}", saved.size(), periodeId);
    }

    public PageResponse<StockIngredientSnapshotDto> getSnapshots(Long periodeId, int page, int size) {
        Page<StockIngredientSnapshot> snapshots = snapshotRepository.findByPeriodeId(
                periodeId,
                PageRequest.of(page, size)
        );
        return PageUtils.toPageResponse(snapshots.map(snapshotMapper::toDto));
    }
    // Cleanup old snapshots (retention policy)
//    @Scheduled(cron = "0 0 2 * * ?") // Daily at 2 AM
//    public void nettoyerAnciensSnapshots() {
//        LocalDateTime cutoff = LocalDateTime.now().minusMonths(24);
//        int deleted = snapshotRepository.deleteByPeriodeDateDebutBefore(cutoff);
//        if (deleted > 0) {
//            log.info("Supprimé {} snapshots anciens de plus de 24 mois", deleted);
//        }
//    }
}