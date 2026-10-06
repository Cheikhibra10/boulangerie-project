// production/service/ProductionService.java
package com.boulangerie.production.service;

import com.boulangerie.production.dto.*;
import com.boulangerie.shared.dto.PageResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;

public interface ProductionService {

    // ===== RÉPARTITION =====
    List<DestinationDto> distribuerProduction(Long lotId, DistribuerProductionRequestDto dto);

    // ===== CONSULTATION =====
    LotProductionDto getLot(Long id);

    PageResponse<LotProductionDto> getLots( int page, int size);

    List<DestinationDto> getDestinationsByLot(Long lotId);

    List<DestinationDto> getDestinationsByLivreurEtDate(Long livreurId, LocalDate date);

    PageResponse<DestinationDto> search(DestinationProductionFilter filter, Pageable pageable);
    PageResponse<LotProductionDto> search(LotProductionFilter filter, Pageable pageable);

}