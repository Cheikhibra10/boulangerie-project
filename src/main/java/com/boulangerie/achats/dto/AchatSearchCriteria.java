package com.boulangerie.achats.dto;

import com.boulangerie.achats.model.Achat;
import com.boulangerie.achats.model.StatutAchat;
import com.boulangerie.achats.repository.AchatSpecifications;
import lombok.Builder;
import lombok.Value;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Value
@Builder
public class AchatSearchCriteria {
    Long fournisseurId;
    StatutAchat statut;
    LocalDateTime dateDebut;
    LocalDateTime dateFin;
    BigDecimal montantMin;
    BigDecimal montantMax;
    Boolean estPaye;
    Boolean estAnnule;
    Boolean estRecu;

    public Specification<Achat> toSpecification() {
        return Specification.where(AchatSpecifications.hasFournisseurId(fournisseurId))
                .and(AchatSpecifications.hasStatut(statut))
                .and(AchatSpecifications.hasDateBetween(dateDebut, dateFin))
                .and(AchatSpecifications.hasMontantTotalBetween(montantMin, montantMax))
                .and(estPaye != null && estPaye ? AchatSpecifications.estPaye() : null)
                .and(estAnnule != null && estAnnule ? AchatSpecifications.estAnnule() : null)
                .and(estRecu != null && estRecu ? AchatSpecifications.estRecu() : null);
    }
}