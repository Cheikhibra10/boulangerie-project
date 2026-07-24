// caisse/model/Caisse.java
package com.boulangerie.comptabilite.model;

import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "caisses")
@Getter
@Setter
@Accessors(chain = true)
public class Caisse extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_ouverture", nullable = false)
    private Instant dateOuverture;

    @Column(name = "date_fermeture")
    private Instant dateFermeture;

    @Column(name = "solde_initial", precision = 15, scale = 2, nullable = false)
    private BigDecimal soldeInitial;

    @Column(name = "solde_final", precision = 15, scale = 2)
    private BigDecimal soldeFinal;

    @Column(name = "solde_physique", precision = 15, scale = 2)
    private BigDecimal soldePhysique;

    @Column(name = "ecart", precision = 15, scale = 2, insertable = false, updatable = false)
    private BigDecimal ecart;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutCaisse statut = StatutCaisse.OUVERTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ouverte_par", nullable = false)
    private Utilisateur ouvertePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fermee_par")
    private Utilisateur fermeePar;

}