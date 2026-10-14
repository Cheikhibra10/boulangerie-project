package com.boulangerie.rh.model;

import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.time.LocalDate;

@Entity
@Table(
        name = "employes",
        indexes = {
                @Index(name = "idx_employe_statut", columnList = "statut"),
                @Index(name = "idx_employe_nom", columnList = "nom"),
                @Index(name = "idx_employe_matricule", columnList = "matricule")
        }
)
@Getter
@Setter
@Accessors(chain = true)
public class Employe extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Business key, generated once from id: Matricule-000001 */
    @Column(unique = true, length = 30)
    private String matricule;

    @Column(nullable = false, length = 100)
    private String nom;

    @Column(nullable = false, length = 100)
    private String prenom;

    @Column(length = 30)
    private String telephone;

    /** Optional for staff without login; filled from Utilisateur when linked */
    @Column(length = 150)
    private String email;

    @Column(name = "date_embauche", nullable = false)
    private LocalDate dateEmbauche;

    @Column(name = "date_sortie")
    private LocalDate dateSortie;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatutEmploye statut = StatutEmploye.ACTIF;

    @Column(name = "utilisateur_id")
    private Long utilisateurId;

    @Column(name = "livreur_id")
    private Long livreurId;

    public String getNomComplet() {
        return prenom + " " + nom;
    }

    public boolean hasCompteUtilisateur() {
        return utilisateurId != null;
    }

    public void assignMatriculeFromId() {
        if (id == null) {
            throw new IllegalStateException(
                    "L'id doit être généré avant d'assigner le matricule.");
        }
        this.matricule = "Matricule-%06d".formatted(id);
    }

    public void unlinkUtilisateur() {
        this.utilisateurId = null;
        // email / nom / prenom kept as HR snapshot
    }
}