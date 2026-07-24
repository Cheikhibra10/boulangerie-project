// administration/service/impl/ProduitServiceImpl.java
package com.boulangerie.administration.service.Impl;

import com.boulangerie.administration.dto.ProduitDto;
import com.boulangerie.administration.mapper.ProduitMapper;
import com.boulangerie.administration.model.CategorieProduit;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.administration.repository.CategorieProduitRepository;
import com.boulangerie.administration.repository.ProduitRepository;
import com.boulangerie.administration.repository.RecetteRepository;
import com.boulangerie.administration.service.ProduitService;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.exception.EntityNotFoundException;
import com.boulangerie.shared.service.impl.AbstractCrudService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProduitServiceImpl
        extends AbstractCrudService<Produit, ProduitDto>
        implements ProduitService {

    private final RecetteRepository recetteRepository;
    private final ProduitRepository repository;
    private final CategorieProduitRepository categorieProduitRepository;

    public ProduitServiceImpl(ProduitRepository repository,
                              ProduitMapper mapper,
                              RecetteRepository recetteRepository, CategorieProduitRepository categorieProduitRepository) {
        super(repository, mapper, Produit.class);
        this.repository = repository;
        this.recetteRepository = recetteRepository;
        this.categorieProduitRepository = categorieProduitRepository;
    }

    @Override
    @Transactional
    public ProduitDto create(ProduitDto dto) {


        if(repository.existsByLibelle(dto.getLibelle())) {

            throw new BadRequestException(
                    "Un produit avec le nom '"
                            + dto.getLibelle()
                            + "' existe déjà."
            );
        }



        Produit produit = mapper.toEntity(dto);



        CategorieProduit categorie =
                categorieProduitRepository.findById(dto.getCategorieId())
                        .orElseThrow(() -> new BadRequestException(
                                        "Catégorie introuvable : " + dto.getCategorieId())
                        );
        produit.setCategorie(categorie);
        return mapper.toDto(repository.save(produit)
        );
    }

    @Override
    public ProduitDto update(Long id, ProduitDto dto) {
        repository.findByLibelle(dto.getLibelle())
                .ifPresent(existing -> {
                    if (!existing.getId().equals(id)) {
                        throw new BadRequestException("Un produit avec le nom '" + dto.getLibelle() + "' existe déjà.");
                    }
                });
        return super.update(id, dto);
    }

    @Override
    public ProduitDto delete(Long id) {
        // Vérifier si le produit a des recettes associées
        if (recetteRepository.existsByProduitIdAndActifTrue(id)) {
            throw new BadRequestException("Impossible de supprimer ce produit car il a des recettes actives.");
        }
        return super.delete(id);
    }

    @Override
    public ProduitDto archive(Long id) {
        // Vérifier si le produit a des recettes actives
        if (recetteRepository.existsByProduitIdAndActifTrue(id)) {
            throw new BadRequestException("Impossible d'archiver ce produit car il a des recettes actives.");
        }
        Produit entity = getEntityById(id);
        entity.setActif(false);
        return mapper.toDto(repository.save(entity));
    }


    @Override
    public Produit findProduitOrThrow(Long id) {
        return repository.findById(id)
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


}