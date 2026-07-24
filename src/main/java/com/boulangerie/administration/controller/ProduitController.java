// administration/controller/ProduitController.java
package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.ProduitDto;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.shared.controller.GenericCrudController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/produits")
@Tag(name = "Produits", description = "Gestion des produits finis")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class ProduitController
        extends GenericCrudController<Produit, ProduitDto> {

    public ProduitController(ProduitService service) {
        super(service);
    }
}