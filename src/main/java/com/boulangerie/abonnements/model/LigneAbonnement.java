package com.boulangerie.abonnements.model;

import com.boulangerie.shared.exception.ConflictException;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;


@Entity
@Table(
        name = "lignes_abonnement",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_abonnement_client",
                        columnNames = {
                                "abonnement_id",
                                "client_id"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
public class LigneAbonnement extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abonnement_id", nullable = false)
    private Abonnement abonnement;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "client_id", nullable = false)
    private Client client;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal prixUnitaire;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal reliquat = BigDecimal.ZERO;

    @OneToMany(mappedBy = "ligne", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<ConsommationJournaliere> consommations = new ArrayList<>();

    public LigneAbonnement(Client client, BigDecimal prixUnitaire) {
        this.client = Objects.requireNonNull(client);
        this.prixUnitaire = validateMontant(prixUnitaire);
        this.reliquat = BigDecimal.ZERO;
    }

    public static LigneAbonnement creer(Client client, BigDecimal prixUnitaire) {
        return new LigneAbonnement(client, prixUnitaire);
    }

    void rattacherA(Abonnement abonnement) {
        this.abonnement = Objects.requireNonNull(abonnement);
    }

    public ConsommationJournaliere enregistrerConsommation(LocalDate date, BigDecimal quantite) {

        verifierConsommationExistante(date);
        ConsommationJournaliere consommation = ConsommationJournaliere.creer(this, date, quantite);
        consommations.add(consommation);
        ajouterDette(calculerMontant(quantite));
        return consommation;
    }


    public BigDecimal calculerReliquatApresPaiement(BigDecimal montant) {

        validateMontant(montant);
        return reliquat.subtract(montant);
    }

    private void ajouterDette(BigDecimal montant) {
        validateMontant(montant);
        reliquat = reliquat.add(montant);
    }

    public void payer(BigDecimal montant) {
        validateMontant(montant);
        reliquat = reliquat.subtract(montant);
    }

    public BigDecimal calculerMontant(BigDecimal quantite) {
        return prixUnitaire.multiply(quantite);
    }

    public boolean estEnDette() {
        return reliquat.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean estCrediteur() {
        return reliquat.compareTo(BigDecimal.ZERO) < 0;
    }

    public BigDecimal getChiffreAffaires() {
        return consommations.stream()
                .map(ConsommationJournaliere::calculerMontant)
                .reduce(
                        BigDecimal.ZERO,
                        BigDecimal::add
                );
    }

    public List<ConsommationJournaliere> getConsommations() {
        return Collections.unmodifiableList(consommations);
    }

    private void verifierConsommationExistante(LocalDate date) {
        boolean existe = consommations.stream().anyMatch(c -> c.getDate().equals(date));
        if (existe) {
            throw new ConflictException("Une consommation existe déjà pour cette date");
        }
    }

    private BigDecimal validateMontant(BigDecimal montant) {
        if (montant == null || montant.signum() <= 0) {
            throw new IllegalArgumentException("Montant invalide");
        }
        return montant;
    }
}