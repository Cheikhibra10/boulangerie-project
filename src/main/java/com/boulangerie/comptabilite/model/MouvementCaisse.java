// caisse/model/MouvementCaisse.java
package com.boulangerie.comptabilite.model;

import com.boulangerie.shared.model.TransactionMonetaire;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

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

}