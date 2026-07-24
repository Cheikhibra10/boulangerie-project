// abonnements/model/VersementAbonnement.java
package com.boulangerie.abonnements.model;

import com.boulangerie.shared.model.TransactionMonetaire;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
@Table(name = "versements_abonnement")
public class VersementAbonnement extends TransactionMonetaire {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "compte_abonnement_id", nullable = false)
    private CompteAbonnement compte;
}