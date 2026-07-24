package com.boulangerie.achats.service;

import com.boulangerie.achats.dto.CreationAchatDto;
import com.boulangerie.achats.dto.LigneAchatRequestDto;
import com.boulangerie.achats.model.Achat;
import com.boulangerie.shared.dto.ValeursStock;
import com.boulangerie.administration.model.Fournisseur;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.administration.repository.FournisseurRepository;
import com.boulangerie.administration.repository.IngredientRepository;
import com.boulangerie.shared.exception.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AchatFactory {

    private final FournisseurRepository fournisseurRepository;
    private final IngredientRepository ingredientRepository;

    /**
     * Creates a complete Achat aggregate with all its lines.
     * This is the ONLY way to create a new Achat.
     */
    public Achat create(CreationAchatDto dto) {
        // 1. Load Fournisseur
        Fournisseur fournisseur = fournisseurRepository.findById(dto.getFournisseurId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Fournisseur introuvable: " + dto.getFournisseurId()
                ));

        // 2. Create empty Achat
        Achat achat = Achat.nouveauAchat(fournisseur);

        // 3. Add all lines
        for (LigneAchatRequestDto ligneDto : dto.getLignes()) {
            Ingredient ingredient = ingredientRepository.findByIdAndActifTrue(ligneDto.getIngredientId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Ingrédient actif introuvable: " + ligneDto.getIngredientId()
                    ));
            achat.ajouterLigne(ingredient, ligneDto.getQuantite(), ligneDto.getPrixUnitaire());
        }
        return achat;
    }
}