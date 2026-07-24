package com.boulangerie.livreurs.model;

import com.boulangerie.shared.model.TransactionMonetaire;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "versement_livreurs")
@Getter
@Setter
@Accessors(chain = true)
public class VersementLivreur extends TransactionMonetaire {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journalier_id", nullable = false, unique = true)
    private CompteLivreurJournalier compteRendu;

}