// caisse/model/Caisse.java
package com.boulangerie.comptabilite.model;

import com.boulangerie.administration.model.Utilisateur;
import com.boulangerie.comptabilite.exception.CaisseFermeeException;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.flywaydb.core.internal.util.StringUtils;

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

    @Column(name = "motif_ecart", length = 250)
    private String motifEcart;

    @Enumerated(EnumType.STRING)
    @Column(name = "statut", nullable = false)
    private StatutCaisse statut = StatutCaisse.OUVERTE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ouverte_par", nullable = false)
    private Utilisateur ouvertePar;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fermee_par")
    private Utilisateur fermeePar;

    public static Caisse ouvrir(
            Utilisateur utilisateur,
            BigDecimal soldeInitial) {

        if (soldeInitial == null || soldeInitial.signum() < 0) {
            throw new BadRequestException("Le solde initial est invalide.");
        }

        Caisse caisse = new Caisse();
        caisse.dateOuverture = Instant.now();
        caisse.soldeInitial = soldeInitial;
        caisse.statut = StatutCaisse.OUVERTE;
        caisse.ouvertePar = utilisateur;

        return caisse;
    }

    public void fermer(
            BigDecimal soldeTheorique,
            BigDecimal soldePhysique,
            String motifEcart,
            Utilisateur utilisateur) {

        verifierOuverte();

        if (soldePhysique == null) {
            throw new BadRequestException("Le solde physique est obligatoire.");
        }

        this.dateFermeture = Instant.now();
        this.soldeFinal = soldeTheorique;
        this.soldePhysique = soldePhysique;
        this.fermeePar = utilisateur;
        if (hasEcart() && !StringUtils.hasText(motifEcart)) {
            throw new BadRequestException(
                    "Un motif est obligatoire lorsqu'un écart de caisse est constaté."
            );
        }
        this.motifEcart = motifEcart;
        this.statut = StatutCaisse.FERMEE;
    }

    public void verifierOuverte() {
        if (statut != StatutCaisse.OUVERTE) {
            throw new CaisseFermeeException();
        }
    }

    public BigDecimal calculerSoldeTheorique(
            BigDecimal totalEntrees,
            BigDecimal totalSorties) {

        return soldeInitial
                .add(totalEntrees)
                .subtract(totalSorties);
    }
    @Transient
    public BigDecimal getEcart() {

        if (soldeFinal == null || soldePhysique == null) {
            return BigDecimal.ZERO;
        }

        return soldePhysique.subtract(soldeFinal);
    }

    public boolean hasEcart() {
        return getEcart().compareTo(BigDecimal.ZERO) != 0;
    }

}