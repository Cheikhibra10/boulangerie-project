// administration/controller/FournisseurController.java
package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.FournisseurDto;
import com.boulangerie.administration.model.Fournisseur;
import com.boulangerie.administration.service.FournisseurService;
import com.boulangerie.shared.controller.GenericCrudController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/fournisseurs")
@Tag(name = "Fournisseurs", description = "Gestion des fournisseurs")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class FournisseurController
        extends GenericCrudController<Fournisseur, FournisseurDto> {

    public FournisseurController(FournisseurService service) {
        super(service);
    }
}