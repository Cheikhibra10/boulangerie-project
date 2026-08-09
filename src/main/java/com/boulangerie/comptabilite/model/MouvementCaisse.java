// caisse/model/MouvementCaisse.java
package com.boulangerie.comptabilite.model;

import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.TransactionMonetaire;
import com.boulangerie.shared.model.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Table(name = "mouvements_caisse")
@Accessors(chain = true)
@Getter
@Setter
public class MouvementCaisse extends TransactionMonetaire {


    @Enumerated(EnumType.STRING)
    @Column(name = "type_mouvement", nullable = false)
    private TypeMouvement typeMouvement;

    @Enumerated(EnumType.STRING)
    @Column(name = "sens", nullable = false)
    private SensMouvement sens;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "caisse_id", nullable = false)
    private Caisse caisse;


    public static MouvementCaisse creer( TypeMouvement type,
                                         SensMouvement sens,
                                         TypePaiement paiement,
                                         Caisse caisse,
                                         String libelle,
                                         BigDecimal montant
    ) {
        if(montant == null || montant.signum() <= 0){
            throw new BadRequestException("Le montant doit être positif.");
        }

        MouvementCaisse mouvement = new MouvementCaisse();
        mouvement.setTypeMouvement(type);
        mouvement.setSens(sens);
        mouvement.setModePaiement(paiement);
        mouvement.setCaisse(caisse);
        mouvement.setLibelle(libelle);
        mouvement.setMontant(montant);
        return mouvement;

    }
}