package com.boulangerie.administration.controller;

import com.boulangerie.administration.dto.ProduitDto;
import com.boulangerie.administration.dto.ProduitMultipartRequest;
import com.boulangerie.administration.dto.ProduitUpdateDto;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.shared.dto.ApiResponse;
import com.boulangerie.shared.dto.PageResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/produits")
@RequiredArgsConstructor
@Tag(name = "Produits", description = "Gestion des produits finis ADMIN-MANAGER")
@PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
public class ProduitController {

    private final ProduitService produitService;

    @Operation(summary = "Créer un produit")
    @RequestBody(
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = ProduitMultipartRequest.class
                    ),
                    encoding = {
                            @Encoding(name = "dto", contentType = MediaType.APPLICATION_JSON_VALUE),
                            @Encoding(name = "image", contentType = MediaType.IMAGE_JPEG_VALUE)
                    }
            )
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ApiResponse<ProduitDto>> create(
            @RequestPart("dto") @Valid ProduitDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Produit créé avec succès",
                                produitService.create(dto, image)
                        )
                );
    }

    @Operation(summary = "Modifier un produit")
    @RequestBody(
            required = true,
            content = @Content(mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                    schema = @Schema(implementation = ProduitMultipartRequest.class
                    ),
                    encoding = {
                            @Encoding(name = "dto", contentType = MediaType.APPLICATION_JSON_VALUE),
                            @Encoding(name = "image", contentType = MediaType.IMAGE_JPEG_VALUE)
                    }
            )
    )
    @PutMapping(value = "/{id}",
                consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<ProduitDto>> update(
            @PathVariable Long id,
            @RequestPart("dto") @Valid ProduitDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image) {

        return ResponseEntity.ok(ApiResponse.success("Produit modifié avec succès", produitService.update(id, dto, image))
        );
    }

    @Operation(summary = "Modifier partiellement un produit")
    @PatchMapping(
            value = "/{id}",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ApiResponse<ProduitDto>> patch(
            @PathVariable Long id,
            @RequestPart(value = "dto", required = false) ProduitUpdateDto dto,
            @RequestPart(value = "image", required = false) MultipartFile image
    ){
        return ResponseEntity.ok(
                ApiResponse.success("Produit modifié avec succès", produitService.patch(id, dto, image))
        );
    }


    @Operation(summary = "Supprimer un produit")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<ProduitDto>> delete(@PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Produit supprimé avec succès", produitService.delete(id))
        );
    }

    @Operation(summary = "Archiver un produit")
    @PatchMapping("/{id}/archive")
    public ResponseEntity<ApiResponse<ProduitDto>> archive(@PathVariable Long id) {

        return ResponseEntity.ok(
                ApiResponse.success("Produit archivé avec succès", produitService.archive(id))
        );
    }

    @Operation(summary = "Trouver un produit")
    @GetMapping("/{id}")
    public ResponseEntity<ProduitDto> getById(@PathVariable Long id) {

        return ResponseEntity.ok(produitService.getById(id));
    }

    @Operation(summary = "Lister les produits")
    @GetMapping

    public ResponseEntity<PageResponse<ProduitDto>> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(produitService.findAll(page, size));
    }

}