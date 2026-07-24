package com.boulangerie.stocks.api;

import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.stocks.dto.StockIngredientSnapshotDto;

public interface SnapshotServiceApi {
    void creerSnapshots(Long periodeId);
    PageResponse<StockIngredientSnapshotDto> getSnapshots(Long periodeId, int page, int size);
}
