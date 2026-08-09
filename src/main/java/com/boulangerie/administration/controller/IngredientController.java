package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.IngredientDto;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.service.IngredientService;
import com.boulangerie.shared.controller.GenericCrudController;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ingredients")
@Tag(name = "Ingrédients", description = "Gestion des ingrédients ADMIN-MANAGER")
@PreAuthorize("hasAnyRole('MANAGER','ADMIN')")
public class IngredientController
        extends GenericCrudController<Ingredient, IngredientDto> {

    public IngredientController(IngredientService service) {
        super(service);
    }
}