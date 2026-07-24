package com.boulangerie.abonnements.model;

import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.Accessors;

import java.util.Objects;

@Entity
@Table(name = "clients", uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_client_telephone",
                        columnNames = "telephone"
                )
        }
)
@Getter
@Setter
@Accessors(chain = true)
public class Client extends AbstractAuditingEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String nom;


    @Column(length = 100)
    private String prenom;


    @Column(nullable = false, length = 20)
    private String telephone;


    @Column(nullable = false)
    private Boolean actif = true;


    public void modifierInformations(
            String nom,
            String prenom,
            String telephone
    ) {

        this.nom = validateRequired(nom, "Nom");
        this.prenom = prenom;
        this.telephone = validateRequired(telephone, "Téléphone");
    }


    public void desactiver() {
        this.actif = false;
    }


    public void activer() {
        this.actif = true;
    }


    public boolean estActif() {
        return Boolean.TRUE.equals(actif);
    }


    private String validateRequired(
            String value,
            String field
    ) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    field + " obligatoire"
            );
        }

        return value.trim();
    }
}