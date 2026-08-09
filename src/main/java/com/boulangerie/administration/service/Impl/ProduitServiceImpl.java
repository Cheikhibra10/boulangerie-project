package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.dto.ProduitDto;
import com.boulangerie.administration.dto.ProduitUpdateDto;
import com.boulangerie.administration.mapper.ProduitMapper;
import com.boulangerie.administration.model.CategorieProduit;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.model.TypeProduit;
import com.boulangerie.administration.repository.CategorieProduitRepository;
import com.boulangerie.administration.repository.ProduitRepository;
import com.boulangerie.administration.repository.RecetteRepository;
import com.boulangerie.administration.storage.dto.ImageUploadResult;
import com.boulangerie.administration.storage.service.ImageStorageService;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.shared.dto.PageResponse;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.utils.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Set;

@Service
@Transactional
public class ProduitServiceImpl implements ProduitService {

    private final RecetteRepository recetteRepository;
    private final ProduitRepository repository;
    private final CategorieProduitRepository categorieProduitRepository;
    private final ImageStorageService imageStorageService;
    private final ProduitMapper mapper;

    public ProduitServiceImpl(ProduitRepository repository,
                              ProduitMapper mapper,
                              RecetteRepository recetteRepository, CategorieProduitRepository categorieProduitRepository, ImageStorageService imageStorageService) {
        this.repository = repository;
        this.recetteRepository = recetteRepository;
        this.categorieProduitRepository = categorieProduitRepository;
        this.imageStorageService = imageStorageService;
        this.mapper = mapper;
    }

    @Transactional
    @Override
    public ProduitDto create(ProduitDto dto, MultipartFile image) {

        if(repository.existsByLibelle(dto.getLibelle())) {
            throw new BadRequestException("Un produit avec le nom '" + dto.getLibelle() + "' existe déjà."
            );
        }

        Produit produit = mapper.toEntity(dto);
        produit.setCategorie(getCategorie(dto.getCategorieId()));
        if (image != null && !image.isEmpty()) {

            ImageUploadResult uploaded = imageStorageService.upload(image);

            produit.setImageUrl(uploaded.url());
            produit.setImagePublicId(uploaded.publicId());
        }
        return mapper.toDto(repository.save(produit)
        );
    }

    @Override
    public ProduitDto update(Long id, ProduitDto dto, MultipartFile image) {
        Produit produit = repository.getProduitByid(id);
        repository.findByLibelle(dto.getLibelle())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Un produit avec le nom '" + dto.getLibelle() + "' existe déjà.");
                    }
                });
        mapper.partialUpdate(produit, dto);

        produit.setCategorie(getCategorie(dto.getCategorieId()));
        if (image != null && !image.isEmpty()) {
            ImageUploadResult uploaded = imageStorageService.replace(produit.getImagePublicId(), image);
            produit.setImageUrl(uploaded.url());
            produit.setImagePublicId(uploaded.publicId());
        }
        repository.save(produit);

        return mapper.toDto(produit);
    }

    @Override
    @Transactional
    public ProduitDto patch(Long id, ProduitUpdateDto dto, MultipartFile image) {

        if (dto == null && (image == null || image.isEmpty())) {
            throw new BadRequestException(
                    "Aucune modification n'a été fournie."
            );
        }

        Produit produit = repository.getProduitByid(id);

        if (dto != null) {
            repository.findByLibelle(dto.getLibelle())
                    .ifPresent(existing -> {
                        if (!existing.getId().equals(id)) {
                            throw new BadRequestException("Un produit avec le nom '" + dto.getLibelle() + "' existe déjà.");
                        }
                    });

            if (dto.getCategorieId() != null) {
                produit.setCategorie(getCategorie(dto.getCategorieId()));
            }

            mapper.partialUpdate(produit,dto);
        }


        if (image != null && !image.isEmpty()) {

            ImageUploadResult uploaded =
                    imageStorageService.replace(
                            produit.getImagePublicId(),
                            image
                    );

            produit.setImageUrl(uploaded.url());
            produit.setImagePublicId(uploaded.publicId());
        }

        return mapper.toDto(repository.save(produit));
    }

    @Override
    public ProduitDto delete(Long id) {
        // Vérifier si le produit a des recettes associées
        if (recetteRepository.existsByProduitIdAndActifTrue(id)) {
            throw new BadRequestException("Impossible de supprimer ce produit car il a des recettes actives.");
        }

        Produit produit = repository.getProduitByid(id);

        if (produit.getImagePublicId() != null) {
            imageStorageService.delete(produit.getImagePublicId());
        }

        repository.delete(produit);
        return  mapper.toDto(produit);
    }

    @Override
    public ProduitDto archive(Long id) {
        // Vérifier si le produit a des recettes actives
        if (recetteRepository.existsByProduitIdAndActifTrue(id)) {
            throw new BadRequestException("Impossible d'archiver ce produit car il a des recettes actives.");
        }
        Produit entity = repository.getProduitByid(id);
        entity.setActif(false);
        return mapper.toDto(repository.save(entity));
    }


    @Override
    public Set<Long> findIdsByType(TypeProduit type) {
        return repository.findIdsByType(type);
    }

    @Override
    public Produit findProduitOrThrow(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable" +id));
    }

    @Override
    public ProduitDto getById(Long id) {
        return repository.findById(id)
                .map(mapper::toDto)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable" +id));
    }

    @Override
    public Long findProduitIdOrThrow(Long id) {
        if (!repository.existsById(id)) {
            throw new EntityNotFoundException("Produit introuvable : " + id);
        }
        return id;
    }

    @Override
    public Long getIdByIdLibelle(String libelle) {
        return repository.findIdByLibelle(libelle)
                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable : " + libelle));
    }

    @Override
    public PageResponse<ProduitDto> findAll(int page, int size) {
        Page<Produit> pageResult = repository.findAll(PageRequest.of(page, size));
        return PageUtils.toPageResponse(pageResult.map(mapper::toDto));

    }

    private CategorieProduit getCategorie(Long id) {

        return categorieProduitRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException(
                                "Catégorie introuvable : " + id
                        ));
    }


}