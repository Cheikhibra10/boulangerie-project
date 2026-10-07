package com.boulangerie.administration.specification;

import com.boulangerie.administration.dto.ProduitFilter;
import com.boulangerie.administration.model.Produit;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

public final class ProduitSpecifications {

    private ProduitSpecifications() {}

    public static Specification<Produit> withFilters(ProduitFilter filter) {
        return Specification
                .<Produit>where(SearchSpecifications.like("libelle", filter.libelle()))
                .and(SearchSpecifications.equal("typeProduit", filter.typeProduit()))
                .and(SearchSpecifications.like("categorie.libelle", filter.categorieLibelle()))
                .and(filter.actif() == null ? null :
                        filter.actif()
                                ? SearchSpecifications.isTrue("actif")
                                : SearchSpecifications.isFalse("actif"));
    }
}