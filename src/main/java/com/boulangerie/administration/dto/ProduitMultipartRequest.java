package com.boulangerie.administration.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@Schema(name = "ProduitMultipartRequest")
public class ProduitMultipartRequest {

    @Schema(
            description = "Données du produit",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    private ProduitDto dto;

    @Schema(
            description = "Image du produit",
            type = "string",
            format = "binary"
    )
    private MultipartFile image;
}