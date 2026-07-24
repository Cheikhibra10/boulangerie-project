package com.boulangerie.abonnements.model;

import com.boulangerie.shared.model.TransactionMonetaire;
import com.boulangerie.shared.model.TypePaiement;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "paiements_abonnement")
public class PaiementAbonnement extends TransactionMonetaire {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ligne_abonnement_id", nullable = false)
    private LigneAbonnement ligne;

    private PaiementAbonnement(
            LigneAbonnement ligne,
            BigDecimal montant,
            TypePaiement modePaiement
    ) {

        super(
                montant,
                modePaiement,
                "Paiement abonnement"
        );

        this.ligne = ligne;
    }



    public static PaiementAbonnement creer(
            LigneAbonnement ligne,
            BigDecimal montant,
            TypePaiement modePaiement
    ) {

        return new PaiementAbonnement(
                ligne,
                montant,
                modePaiement
        );
    }
}