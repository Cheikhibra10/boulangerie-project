package com.boulangerie.stocks.api;

import com.boulangerie.stocks.dto.ReceptionStockLine;
import com.boulangerie.stocks.dto.RetourStockLine;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface StockManagement {

    void receptionnerAchat(Long achatId, List<ReceptionStockLine> lignes);
    void retournerAchat(Long achatId, List<RetourStockLine> lignes);
}