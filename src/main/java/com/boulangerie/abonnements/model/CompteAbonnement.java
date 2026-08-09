package com.boulangerie.abonnements.model;


import com.boulangerie.abonnements.exception.SoldeCompteAbonnementInsuffisantException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;


@Entity
@Table(
        name = "comptes_abonnement"
)
@Getter
@Setter
@Accessors(chain = true)
public class CompteAbonnement extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abonnement_id", nullable = false, unique = true)
    private Abonnement abonnement;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal soldeActuel = BigDecimal.ZERO;

    public void crediter(BigDecimal montant) {
        verifierMontant(montant);
        soldeActuel = soldeActuel.add(montant);
    }



    public void debiter(BigDecimal montant) {
        verifierMontant(montant);
        if(!peutVerser(montant)) {
            throw new SoldeCompteAbonnementInsuffisantException(abonnement.getId(), soldeActuel, montant
            );
        }
        soldeActuel = soldeActuel.subtract(montant);
    }


    public boolean peutVerser(BigDecimal montant) {
        return soldeActuel.compareTo(montant) >= 0;
    }

    private void verifierMontant(BigDecimal montant) {
        if (montant == null || montant.signum() <= 0) {
            throw new IllegalArgumentException("Montant invalide");
        }
    }
}