package com.boulangerie.administration.dto;

import com.boulangerie.administration.model.RoleUtilisateur;
import jakarta.validation.constraints.*;
import lombok.*;


@Getter
@Setter
public class RegisterUtilisateurRequestDto {

    @NotBlank(message = "Le prénom est obligatoire")
    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @NotBlank(message = "L'email est obligatoire")
    @Email(message = "Format d'email invalide")
    private String email;

    @Size(max = 12, message = "Le téléphone ne doit pas dépasser 12 caractères")
    @Pattern(
            regexp = "^(?:\\+221)?(?:7(?:0|1|2|5|6|7|8|9)\\d{7}|3\\d{8})$",
            message = "Numéro de téléphone invalide"
    )
    private String telephone;

    @NotBlank(message = "Le mot de passe est obligatoire")
    @Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
    private String password;

    @NotNull(message = "Le rôle est obligatoire")
    private RoleUtilisateur role;
}