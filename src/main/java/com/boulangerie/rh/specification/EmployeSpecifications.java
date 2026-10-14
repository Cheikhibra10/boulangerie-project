package com.boulangerie.rh.specification;

import com.boulangerie.rh.dto.EmployeFilter;
import com.boulangerie.rh.model.Employe;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

public final class EmployeSpecifications {

    private EmployeSpecifications() {}

    public static Specification<Employe> withFilters(EmployeFilter filter) {
        return Specification
                .<Employe>where(SearchSpecifications.like("nom", filter.nom()))
                .and(SearchSpecifications.like("prenom", filter.prenom()))
                .and(SearchSpecifications.like("matricule", filter.matricule()))
                .and(SearchSpecifications.equal("statut", filter.statut()));
    }
}