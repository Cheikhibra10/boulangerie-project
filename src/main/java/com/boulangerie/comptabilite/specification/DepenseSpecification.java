package com.boulangerie.comptabilite.specification;

import com.boulangerie.comptabilite.model.DepensePeriode;
import org.springframework.data.jpa.domain.Specification;

public class DepenseSpecification {

    public static Specification<DepensePeriode> categorie(Long categorieId) {

        return (root, query, cb) ->

                categorieId == null

                        ? cb.conjunction()

                        : cb.equal(root.get("categorie").get("id"), categorieId);
    }

    public static Specification<DepensePeriode> periode(Long periodeId) {

        return (root, query, cb) ->

                periodeId == null

                        ? cb.conjunction()

                        : cb.equal(root.get("periode").get("id"), periodeId);
    }
}