package com.boulangerie.administration.service;

import com.boulangerie.administration.dto.CategorieDepenseDto;
import com.boulangerie.administration.model.CategorieDepense;
import com.boulangerie.shared.service.DefaultService;

public interface CategorieDepenseService extends DefaultService<CategorieDepense, CategorieDepenseDto> {
    CategorieDepense findCategorieDepenseOrThrow(Long categorieId);
}