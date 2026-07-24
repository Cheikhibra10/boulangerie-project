// administration/controller/CategorieProduitController.java
package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.CategorieProduitDto;
import com.boulangerie.administration.model.CategorieProduit;
import com.boulangerie.administration.service.CategorieProduitService;
import com.boulangerie.shared.controller.GenericCrudController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories-produit")
@Tag(name = "Catégories Produit", description = "Gestion des catégories de produits")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class CategorieProduitController
        extends GenericCrudController<CategorieProduit, CategorieProduitDto> {

    public CategorieProduitController(CategorieProduitService service) {
        super(service);
    }
}