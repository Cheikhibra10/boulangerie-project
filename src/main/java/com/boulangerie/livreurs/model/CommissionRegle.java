// livreurs/model/CommissionRegle.java
package com.boulangerie.livreurs.model;

import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "commissions_regle")
@Getter
@Setter
@Accessors(chain = true)
public class CommissionRegle extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "produit_id", nullable = false)
    private Long produitId;

    @Column(name = "livreur_id", nullable = false)
    private Long livreurId;

    @Column(name = "montant_par_unite", precision = 10, scale = 2, nullable = false)
    private BigDecimal montantParUnite;

    @Column(name = "date_debut", nullable = false)
    private LocalDate dateDebut;

    @Column(name = "date_fin")
    private LocalDate dateFin;

    public static CommissionRegle creer(
            Long livreurId,
            Long produitId,
            BigDecimal montant,
            LocalDate debut) {

        CommissionRegle regle = new CommissionRegle();

        regle.livreurId = livreurId;
        regle.produitId = produitId;
        regle.montantParUnite = montant;
        regle.dateDebut = debut;

        return regle;
    }

    public void fermer(LocalDate dateFin){
        this.dateFin = dateFin;
    }
}