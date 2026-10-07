package com.boulangerie.ventes.specification;

import com.boulangerie.shared.specification.SearchSpecifications;
import com.boulangerie.ventes.dto.VenteBoutiqueFilter;
import com.boulangerie.ventes.model.StatutVente;
import com.boulangerie.ventes.model.VenteBoutique;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public final class VenteBoutiqueSpecifications {

    private VenteBoutiqueSpecifications() {}

    public static Specification<VenteBoutique> withFilters(VenteBoutiqueFilter filter) {
        return Specification
                .<VenteBoutique>where(SearchSpecifications.equal("statut", filter.statut()))
                .and(utilisateurNomContains(filter.utilisateurNom()))
                .and(dateBetween(filter.dateDebut(), filter.dateFin()));
    }

    public static Specification<VenteBoutique> byStatut(StatutVente statut) {
        return SearchSpecifications.equal("statut", statut);
    }

    public static Specification<VenteBoutique> utilisateurNomContains(String nom) {
        return (root, query, cb) -> {
            if (nom == null || nom.isBlank()) {
                return null;
            }
            String pattern = "%" + nom.toLowerCase().trim() + "%";
            // Adapt field names to your Utilisateur entity (nom, username, email...)
            return cb.or(
                    cb.like(cb.lower(root.get("utilisateur").get("nom")), pattern),
                    cb.like(cb.lower(root.get("utilisateur").get("prenom")), pattern)
            );
        };
    }

    public static Specification<VenteBoutique> dateBetween(LocalDate start, LocalDate end) {
        return (root, query, cb) -> {
            if (start == null && end == null) {
                return null;
            }
            if (start != null && end != null) {
                return cb.between(root.get("date"), start, end);
            }
            if (start != null) {
                return cb.greaterThanOrEqualTo(root.get("date"), start);
            }
            return cb.lessThanOrEqualTo(root.get("date"), end);
        };
    }
}