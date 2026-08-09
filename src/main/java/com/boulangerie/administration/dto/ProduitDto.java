package com.boulangerie.administration.dto;

import com.boulangerie.shared.dto.AbstractAuditingDto;
import jakarta.persistence.Column;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProduitDto extends AbstractAuditingDto {

    private Long id;

    @NotBlank(message = "Le nom est obligatoire")
    @Size(max = 100, message = "Le libelle ne doit pas dépasser 100 caractères")
    private String libelle;

    private String imageUrl;

    @NotNull(message = "Le prix détail est obligatoire")
    @Positive(message = "Le prix détail doit être positif")
    @DecimalMin(value = "0.00", message = "Le prix détail doit être supérieur à 0")
    private BigDecimal prixDetail;

    @Positive(message = "Le prix gros doit être positif")
    @DecimalMin(value = "0.00", message = "Le prix gros doit être supérieur à 0")
    private BigDecimal prixGros;

    @Positive(message = "Le prix livreur doit être positif")
    @DecimalMin(value = "0.00", message = "Le prix livreur doit être supérieur à 0")
    private BigDecimal prixLivreur;

    @NotNull(message = "La catégorie est obligatoire")
    private Long categorieId;

    private Boolean actif;

}