package com.boulangerie.administration.model;

import com.boulangerie.shared.model.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "fournisseurs")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@Accessors(chain = true)
public class Fournisseur extends Personne implements GenericEntity<Fournisseur> {


    @Override
    public Fournisseur createNewInstance() {
        return new Fournisseur();
    }
}