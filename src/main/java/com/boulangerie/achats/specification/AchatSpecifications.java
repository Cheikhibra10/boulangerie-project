package com.boulangerie.achats.specification;

import com.boulangerie.achats.dto.AchatFilter;
import com.boulangerie.achats.model.Achat;
import com.boulangerie.shared.specification.SearchSpecifications;
import org.springframework.data.jpa.domain.Specification;

public final class AchatSpecifications {

    private AchatSpecifications() {}

    public static Specification<Achat> withFilters(AchatFilter filter) {
        return Specification
                .<Achat>where(SearchSpecifications.like("fournisseur.nom", filter.fournisseurNom()))
                .and(SearchSpecifications.equal("statutReception", filter.statutReception()))
                .and(SearchSpecifications.equal("statutPaiement", filter.statutPaiement()))
                .and(SearchSpecifications.equal("statutAchat", filter.statutAchat()));
    }
}