package com.boulangerie.achats.model;

import com.boulangerie.achats.dto.ReceptionLigneDto;
import com.boulangerie.achats.dto.RetourLigneDto;
import com.boulangerie.achats.exception.*;
import com.boulangerie.administration.model.Fournisseur;
import com.boulangerie.administration.model.Ingredient;
import com.boulangerie.shared.exception.BadRequestException;
import com.boulangerie.shared.model.TypePaiement;
import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Entity
@Table(name = "achats")
@Getter
@Setter(AccessLevel.PACKAGE) // Only aggregate can modify
public class Achat extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fournisseur_id", nullable = false)
    private Fournisseur fournisseur;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutReception statutReception = StatutReception.EN_ATTENTE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutPaiement statutPaiement = StatutPaiement.NON_PAYE;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatutAchat statutAchat = StatutAchat.ACTIF;

    @Column(name = "montant_total", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantTotal = BigDecimal.ZERO;

    @Column(name = "montant_paye", precision = 15, scale = 2, nullable = false)
    private BigDecimal montantPaye = BigDecimal.ZERO;

    @OneToMany(mappedBy = "achat", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<LigneAchat> lignes = new ArrayList<>();

    @OneToMany(mappedBy = "achat", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PaiementFournisseur> paiementFournisseurs = new ArrayList<>();

    // ===== FACTORY METHOD (Package-Private) =====
    public static Achat nouveauAchat(Fournisseur fournisseur) {
        Achat achat = new Achat();
        achat.fournisseur = fournisseur;
        achat.statutAchat = StatutAchat.ACTIF;
        achat.statutReception = StatutReception.EN_ATTENTE;
        achat.statutPaiement = StatutPaiement.NON_PAYE;
        achat.montantTotal = BigDecimal.ZERO;
        achat.montantPaye = BigDecimal.ZERO;

        return achat;
    }

    // ===== BUSINESS METHODS =====
    public void ajouterLigne(Ingredient ingredient, BigDecimal quantite, BigDecimal prixUnitaire) {
        validateModifiable();
        validatePositive(quantite, "Quantité");
        validatePositive(prixUnitaire, "Prix unitaire");

        LigneAchat ligne = LigneAchat.creer(this, ingredient, quantite, prixUnitaire);
        this.lignes.add(ligne);
        recalculerMontantTotal();
    }

    public void modifierLigne(Long ligneId, BigDecimal nouvelleQuantite, BigDecimal nouveauPrix) {
        validateModifiable();
        validatePositive(nouvelleQuantite, "Quantité");
        validatePositive(nouveauPrix, "Prix unitaire");

        LigneAchat ligne = findLigneOrThrow(ligneId);
        ligne.modifier(nouvelleQuantite, nouveauPrix);
        recalculerMontantTotal();
    }

    public void supprimerLigne(Long ligneId) {
        validateModifiable();
        LigneAchat ligne = findLigneOrThrow(ligneId);
        this.lignes.remove(ligne);
        recalculerMontantTotal();
    }

    public void retourner(List<RetourLigneDto> retours) {

        validateRetourPossible();

        Map<Long, RetourLigneDto> map = retours.stream()
                        .collect(Collectors.toMap(
                                RetourLigneDto::ligneId,
                                Function.identity()
                        ));
        for (RetourLigneDto dto : retours) {
            LigneAchat ligne = findLigneOrThrow(dto.ligneId());
            ligne.retourner(dto.quantiteRetournee(), dto.motif());
        }
        verifierCohérencePaiement();
    }

    public void recevoir(List<ReceptionLigneDto> receptions) {

        validateReceptionPossible();

        if (receptions.size() != lignes.size()) {
            throw new BadRequestException("Toutes les lignes doivent être réceptionnées.");
        }

        Map<Long, ReceptionLigneDto> map = receptions.stream()
                .collect(Collectors.toMap(
                        ReceptionLigneDto::ligneId,
                        Function.identity()
                ));

        for (LigneAchat ligne : lignes) {
            ReceptionLigneDto dto = map.get(ligne.getId());

            if (dto == null) {
                throw new BadRequestException("Ligne manquante : " + ligne.getId());
            }

            ligne.recevoir(
                    dto.quantiteRecue(),
                    dto.quantiteRefusee(),
                    dto.motifRefus()
            );
        }
        verifierCohérencePaiement();
        mettreAJourStatutReception();
    }

    public PaiementFournisseur enregistrerPaiement(BigDecimal montant, TypePaiement modePaiement) {
        validateNonAnnule();
        validateMontantPayable(montant);

        PaiementFournisseur paiement = PaiementFournisseur.creerPaiement(
                this, montant, modePaiement
        );

        this.paiementFournisseurs.add(paiement);
        this.montantPaye = this.montantPaye.add(montant);
        verifierCohérencePaiement();
        mettreAJourStatutPaiement();
        return paiement;
    }

    public void annuler() {
        if (estRecu()) {
            throw new AchatImpossibleAnnulerException(this.id, statutReception);
        }
        this.statutAchat = StatutAchat.ANNULE;
    }

    // ===== QUERY METHODS =====

    private void mettreAJourStatutReception() {
        if (lignes.stream().allMatch(LigneAchat::estTotalementRecue)) {
            statutReception = StatutReception.COMPLETE;
        } else if (lignes.stream().anyMatch(l -> l.getQuantiteRecue().compareTo(BigDecimal.ZERO) > 0)) {
            statutReception = StatutReception.PARTIELLE;
        } else {
            statutReception = StatutReception.EN_ATTENTE;
        }
    }

    private void verifierCohérencePaiement() {

        if (montantPaye.compareTo(getMontantRecu()) > 0) {
            throw new IllegalStateException(
                    "Le montant payé dépasse le montant reçu."
            );
        }
    }
    public BigDecimal getRestantDu() {
        BigDecimal restant = getMontantRecu().subtract(montantPaye);
        return restant.max(BigDecimal.ZERO);
    }

    public boolean estPaye() {
        return statutPaiement == StatutPaiement.PAYE;
    }

    public boolean estRecu() {
        return statutReception != StatutReception.EN_ATTENTE;
    }

    public boolean estReceptionComplete() {
        return statutReception == StatutReception.COMPLETE;
    }

    public boolean estActif() {
        return statutAchat == StatutAchat.ACTIF;
    }

    public boolean estAnnule() {
        return this.statutAchat == StatutAchat.ANNULE;
    }

    public boolean estModifiable() {
        return estActif() && statutReception == StatutReception.EN_ATTENTE || statutReception == StatutReception.PARTIELLE;
    }

    public LigneAchat getLigne(Long ligneId) {
        return lignes.stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() ->
                        new LigneAchatNotFoundException(ligneId, id));
    }

    public List<LigneAchat> getLignes() {
        return Collections.unmodifiableList(this.lignes);
    }

    public List<PaiementFournisseur> getPaiementFournisseurs() {
        return Collections.unmodifiableList(this.paiementFournisseurs);
    }

    private void recalculerMontantTotal() {
        this.montantTotal = this.lignes.stream()
                .map(LigneAchat::getMontantCommande)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getMontantRecu() {
        return lignes.stream()
                .map(LigneAchat::getMontantAccepte)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }



    private void validateReceptionPossible() {

        if (!estActif()) {
            throw new AchatAnnuleException(id);
        }

        if (estReceptionComplete()) {
            throw new AchatDejaRecuException(id);
        }

        if (lignes.isEmpty()) {
            throw new AchatSansLigneException(id);
        }
    }

    private void validateModifiable() {
        if (!estModifiable()) {
            throw new AchatNonModifiableException(this.id, this.statutReception);
        }
    }

    private void validateNonAnnule() {
        if (this.statutAchat == StatutAchat.ANNULE) {
            throw new AchatAnnuleException(this.id);
        }
    }

    private void validateRetourPossible() {

        if (!estActif()) {
            throw new AchatAnnuleException(id);
        }

        if (!estRecu()) {
            throw new BadRequestException("Impossible de retourner un achat non réceptionné.");
        }
    }

    private void validateMontantPayable(BigDecimal montant) {
        if (montant == null || montant.compareTo(BigDecimal.ZERO) <= 0) {
            throw new MontantInvalideException("Le montant doit être positif");
        }
        if (montant.compareTo(getRestantDu()) > 0) {
            throw new MontantExcedentaireException(this.id, montant, getRestantDu());
        }
    }

    private void mettreAJourStatutPaiement() {
        BigDecimal montantRecu = getMontantRecu();

        if (montantPaye.signum() == 0) {
            statutPaiement = StatutPaiement.NON_PAYE;
            return;
        }

        if (montantPaye.compareTo(montantRecu) == 0) {
            statutPaiement = StatutPaiement.PAYE;
            return;
        }
        statutPaiement = StatutPaiement.PARTIEL;
    }

    private LigneAchat findLigneOrThrow(Long ligneId) {
        return this.lignes.stream()
                .filter(l -> l.getId().equals(ligneId))
                .findFirst()
                .orElseThrow(() -> new LigneAchatNotFoundException(ligneId, this.id));
    }

    private void validatePositive(BigDecimal value, String fieldName) {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValeurInvalideException(fieldName + " doit être positive");
        }
    }
}