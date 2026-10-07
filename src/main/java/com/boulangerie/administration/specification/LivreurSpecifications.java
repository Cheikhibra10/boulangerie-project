package com.boulangerie.administration.specification;

import com.boulangerie.administration.dto.LivreurFilter;
import com.boulangerie.administration.model.Livreur;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

public final class LivreurSpecifications {

    private LivreurSpecifications() {}

    public static Specification<Livreur> withFilters(LivreurFilter filter) {
        return Specification
                .<Livreur>where(SearchSpecifications.like("nom", filter.nom()))
                .and(SearchSpecifications.like("prenom", filter.prenom()))
                .and(SearchSpecifications.like("telephone", filter.telephone()))
                .and(filter.actif() == null ? null :
                        filter.actif()
                                ? SearchSpecifications.isTrue("actif")
                                : SearchSpecifications.isFalse("actif"));
    }
}