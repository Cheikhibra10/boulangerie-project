package com.boulangerie.abonnements.service;

import com.boulangerie.abonnements.dto.ConsommationImportResultDto;
import org.springframework.web.multipart.MultipartFile;

import java.time.YearMonth;

public interface CsvImportService {

    ConsommationImportResultDto importerConsommationMensuelle(
            MultipartFile fichier,
            YearMonth periode
    );
}