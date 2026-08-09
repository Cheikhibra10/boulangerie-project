//package com.boulangerie.administration.internal;
//
//import com.boulangerie.administration.api.ProduitApi;
//import com.boulangerie.administration.model.Produit;
//import com.boulangerie.administration.repository.ProduitRepository;
//import com.boulangerie.shared.exception.EntityNotFoundException;
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//
//@Service
//@RequiredArgsConstructor
//public class ProduitApiImpl implements ProduitApi {
//
//    private final ProduitRepository produitRepository;
//    @Override
//    public Produit findProduitOrThrow(Long id) {
//        return produitRepository.findById(id)
//                .orElseThrow(() -> new EntityNotFoundException("Produit introuvable"));
//    }
//}
