package com.boulangerie.administration.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class PersonneDto extends AbstractAuditingDto {

    private Long id;

    @Size(max = 100, message = "Le prénom ne doit pas dépasser 100 caractères")
    private String prenom;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le nom ne doit pas dépasser 100 caractères")
    private String nom;

    @Size(max = 12, message = "Le téléphone ne doit pas dépasser 12 caractères")
    @Pattern(
            regexp = "^(?:\\+221)?(?:7(?:0|1|2|5|6|7|8|9)\\d{7}|3\\d{8})$",
            message = "Numéro de téléphone invalide"
    )
    private String telephone;

    private Boolean actif;

}