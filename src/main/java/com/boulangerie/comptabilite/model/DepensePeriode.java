// caisse/model/DepensePeriode.java
package com.boulangerie.comptabilite.model;

import com.boulangerie.administration.model.CategorieDepense;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import com.boulangerie.shared.model.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "depenses_periode")
@Getter
@Setter
@Accessors(chain = true)
public class DepensePeriode extends AbstractAuditingEntity implements GenericEntity<DepensePeriode> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "periode_id", nullable = false)
    private Periode periode;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mouvement_id", unique = true, nullable = false)
    private MouvementCaisse mouvement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = false)
    private CategorieDepense categorie;

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public DepensePeriode createNewInstance() {
        return new DepensePeriode();
    }
}