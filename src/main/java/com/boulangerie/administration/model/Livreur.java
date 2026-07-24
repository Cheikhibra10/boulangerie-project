package com.boulangerie.administration.model;

import com.boulangerie.shared.model.GenericEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.PrimaryKeyJoinColumn;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "livreurs")
@PrimaryKeyJoinColumn(name = "id")
@Getter
@Setter
@Accessors(chain = true)
public class Livreur extends Personne implements GenericEntity<Livreur> {

    @Override
    public Livreur createNewInstance() {
        return new Livreur();
    }
}