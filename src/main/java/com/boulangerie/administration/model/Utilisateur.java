package com.boulangerie.administration.model;

import com.boulangerie.shared.model.AbstractAuditingEntity;
import com.boulangerie.shared.model.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(
        name = "utilisateurs",
        uniqueConstraints = {
                @UniqueConstraint(name="uk_utilisateur_email", columnNames="email"),
                @UniqueConstraint(name="uk_utilisateur_keycloak", columnNames="keycloak_id")
        }
)
@Getter
@Setter
@Accessors(chain = true)
public class Utilisateur extends AbstractAuditingEntity implements GenericEntity<Utilisateur> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable=false, length=100)
    private String nom;
    @Column(length=100)
    private String prenom;
    @Column(length=20)
    private String telephone;

    @Column(name="keycloak_id", nullable=false, length=100)
    private String keycloakId;

    @Column(nullable=false, length=255)
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(nullable=false)
    private RoleUtilisateur role;


    @Column(nullable=false)
    private Boolean actif = true;



    @Override
    public Utilisateur createNewInstance() {
        return new Utilisateur();
    }
}