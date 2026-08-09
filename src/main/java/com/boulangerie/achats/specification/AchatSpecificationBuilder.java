package com.boulangerie.achats.specification;

import com.boulangerie.achats.dto.AchatSearchRequest;
import com.boulangerie.achats.model.Achat;
import com.boulangerie.achats.repository.AchatSpecifications;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

public final class AchatSpecificationBuilder {

    public static Specification<Achat> build(AchatSearchRequest request) {

        Specification<Achat> specification = Specification.where(null);

        specification = specification.and(
                AchatSpecifications.hasFournisseurId(request.getFournisseurId()));

        specification = specification.and(
                AchatSpecifications.hasStatut(request.getStatut()));

        specification = specification.and(
                AchatSpecifications.hasDateBetween(
                        request.getDateDebut(),
                        request.getDateFin()));

        specification = specification.and(
                AchatSpecifications.hasMontantTotalBetween(
                        request.getMontantMin(),
                        request.getMontantMax()));

        if (Boolean.TRUE.equals(request.getEstPaye())) {
            specification = specification.and(
                    AchatSpecifications.estPaye());
        }

        if (Boolean.TRUE.equals(request.getEstAnnule())) {
            specification = specification.and(
                    AchatSpecifications.estAnnule());
        }

        if (Boolean.TRUE.equals(request.getEstRecu())) {
            specification = specification.and(
                    AchatSpecifications.estRecu());
        }

        return specification;
    }

    private AchatSpecificationBuilder() {
    }
}