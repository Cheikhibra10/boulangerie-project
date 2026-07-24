// livreurs/model/CompteLivreur.java
package com.boulangerie.livreurs.model;

import com.boulangerie.administration.model.Livreur;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Table(name = "comptes_livreur")
@Getter
@Setter
@Accessors(chain = true)
public class CompteLivreur extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column( unique = true, nullable = false)
    private Long livreurId;
    @Column(name = "solde_actuel", precision = 15, scale = 2, nullable = false)
    private BigDecimal soldeActuel = BigDecimal.ZERO;
}