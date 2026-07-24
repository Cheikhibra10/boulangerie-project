package com.boulangerie.achats.model;

import com.boulangerie.shared.model.TransactionMonetaire;
import com.boulangerie.shared.model.TypePaiement;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "paiements_fournisseurs")
@Getter
@Setter(AccessLevel.PACKAGE)
@NoArgsConstructor
@AllArgsConstructor
public class PaiementFournisseur extends TransactionMonetaire {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "achat_id", nullable = false)
    private Achat achat;

    public static PaiementFournisseur creerPaiement(Achat achat, BigDecimal montant,
                                             TypePaiement modePaiement) {
        PaiementFournisseur paiement = new PaiementFournisseur();
        paiement.setMontant(montant);
        paiement.setModePaiement(modePaiement);
        paiement.setLibelle("Paiement Fournisseur - " + achat.getFournisseur().getNom());
        paiement.achat = achat;
        return paiement;
    }
}