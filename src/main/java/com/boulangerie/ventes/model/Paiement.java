package com.boulangerie.ventes.model;

import com.boulangerie.shared.model.TransactionMonetaire;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name="paiements")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Paiement extends TransactionMonetaire {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="vente_id")
    private VenteBoutique vente;
}