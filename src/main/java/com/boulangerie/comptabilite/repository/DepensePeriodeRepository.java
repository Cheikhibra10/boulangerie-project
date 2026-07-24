// caisse/repository/DepensePeriodeRepository.java
package com.boulangerie.comptabilite.repository;

import com.boulangerie.administration.model.CategorieDepense;
import com.boulangerie.comptabilite.model.DepensePeriode;
import com.boulangerie.shared.repository.GenericRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DepensePeriodeRepository extends GenericRepository<DepensePeriode> {

    List<DepensePeriode> findByCategorie(CategorieDepense categorie);
}