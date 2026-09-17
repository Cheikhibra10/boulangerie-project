package com.boulangerie.achats.repository;

import com.boulangerie.achats.model.Achat;
import com.boulangerie.achats.model.StatutAchat;
import com.boulangerie.achats.model.StatutReception;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class AchatSpecifications {

    private AchatSpecifications() {
        // Utility class
    }

    public static Specification<Achat> hasFournisseurId(Long fournisseurId) {
        return (root, query, cb) -> {
            if (fournisseurId == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("fournisseur").get("id"), fournisseurId);
        };
    }

    public static Specification<Achat> hasStatut(StatutAchat statut) {
        return (root, query, cb) -> {
            if (statut == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("statutAchat"), statut);
        };
    }

    public static Specification<Achat> hasDateBetween(LocalDateTime debut, LocalDateTime fin) {
        return (root, query, cb) -> {
            if (debut == null || fin == null) {
                return cb.conjunction();
            }
            return cb.between(root.get("createdAt"), debut, fin);
        };
    }

    public static Specification<Achat> hasMontantTotalBetween(BigDecimal min, BigDecimal max) {
        return (root, query, cb) -> {
            if (min == null && max == null) {
                return cb.conjunction();
            }
            if (min != null && max != null) {
                return cb.between(root.get("montantTotal"), min, max);
            }
            if (min != null) {
                return cb.greaterThanOrEqualTo(root.get("montantTotal"), min);
            }
            return cb.lessThanOrEqualTo(root.get("montantTotal"), max);
        };
    }

    public static Specification<Achat> hasMontantPayeGreaterThan(BigDecimal montant) {
        return (root, query, cb) -> {
            if (montant == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(root.get("montantPaye"), montant);
        };
    }

    public static Specification<Achat> estPaye() {
        return (root, query, cb) -> {
            // Achat is paid when montantPaye >= montantTotal
            return cb.greaterThanOrEqualTo(
                    root.get("montantPaye"),
                    root.get("montantTotal")
            );
        };
    }

    public static Specification<Achat> estAnnule() {
        return (root, query, cb) -> 
            cb.equal(root.get("statutAchat"), StatutAchat.ANNULE);
    }

    public static Specification<Achat> estRecu() {
        return (root, query, cb) -> 
            cb.equal(root.get("statutReception"), StatutReception.COMPLETE);
    }

    public static Specification<Achat> estEnAttente() {
        return (root, query, cb) -> 
            cb.equal(root.get("statutReception"), StatutReception.EN_ATTENTE);
    }

    public static Specification<Achat> estPartiel() {
        return (root, query, cb) -> 
            cb.equal(root.get("statutReception"), StatutReception.PARTIELLE);
    }
}