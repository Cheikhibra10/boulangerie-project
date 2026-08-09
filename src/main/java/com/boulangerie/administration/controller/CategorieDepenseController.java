package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.CategorieDepenseDto;
import com.boulangerie.administration.model.CategorieDepense;
import com.boulangerie.administration.service.CategorieDepenseService;
import com.boulangerie.shared.controller.GenericCrudController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories-depense")
@Tag(name = "Catégories Dépense", description = "Gestion des catégories de dépenses ADMIN-MANAGER")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class CategorieDepenseController
        extends GenericCrudController<CategorieDepense, CategorieDepenseDto> {

    public CategorieDepenseController(CategorieDepenseService service) {
        super(service);
    }
}