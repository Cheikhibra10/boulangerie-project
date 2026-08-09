package com.boulangerie.abonnements.model;


import com.boulangerie.abonnements.exception.DepassementQuantiteAbonnementException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;


@Entity
@Table(name = "abonnements")
@Getter
@Setter
@Accessors(chain = true)
public class Abonnement extends AbstractAuditingEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;

    private String adresse;

    @Column(nullable = false)
    private LocalDate dateDebut;

    private LocalDate dateFin;

    @Column(nullable = false)
    private Long livreurId;

    @Column(nullable = false)
    private Boolean actif = true;

    @OneToMany(mappedBy = "abonnement", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<LigneAbonnement> lignes = new ArrayList<>();

    @OneToOne(mappedBy = "abonnement", cascade = CascadeType.ALL, orphanRemoval = true, optional = false)
    private CompteAbonnement compte;


    public LigneAbonnement ajouterClient(Client client, BigDecimal prixUnitaire) {
        verifierActif();
        verifierClientAbsent(client);
        LigneAbonnement ligne = LigneAbonnement.creer(client, prixUnitaire);
        ligne.rattacherA(this);
        lignes.add(ligne);
        return ligne;
    }

    public boolean estValidePour(LocalDate date) {
        if(!actif) {return false;}
        if(date.isBefore(dateDebut)) {
            return false;
        }
        return dateFin == null || !date.isAfter(dateFin);
    }

    public BigDecimal getChiffreAffaires() {
        return lignes.stream()
                .map(LigneAbonnement::getChiffreAffaires)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void desactiver() {
        actif = false;
    }

    public boolean estActif() {
        return Boolean.TRUE.equals(actif);
    }


    private void verifierClientAbsent(Client client) {
        boolean existe = lignes.stream()
                        .anyMatch(
                                ligne ->
                                        ligne.getClient()
                                                .getId()
                                                .equals(client.getId())
                        );

        if(existe) {
            throw new IllegalStateException("Client déjà abonné");
        }
    }

    public void transfererVersBoulangerie(BigDecimal montant) {
        compte.debiter(montant);
    }

    public void verifierActif() {

        if(!estActif()) {
            throw new IllegalStateException("Abonnement inactif");
        }
    }

    public BigDecimal getQuantiteConsommee(LocalDate date) {
        return lignes.stream()
                .flatMap(l -> l.getConsommations().stream())
                .filter(c -> c.getDate().equals(date))
                .map(ConsommationJournaliere::getQuantite)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void verifierQuantiteDisponible(
            LocalDate date,
            BigDecimal nouvelleQuantite,
            BigDecimal quantiteDistribuee
    )  {
        BigDecimal quantiteConsommee = getQuantiteConsommee(date);
        BigDecimal total = quantiteConsommee.add(nouvelleQuantite);
        if (total.compareTo(quantiteDistribuee) > 0) {
            throw new DepassementQuantiteAbonnementException(
                    this.id,
                    quantiteDistribuee,
                    total
            );
        }
    }
}