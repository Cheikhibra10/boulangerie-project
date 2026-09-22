package com.boulangerie.administration.model;

import com.boulangerie.shared.model.AbstractAuditingEntity;
import com.boulangerie.shared.model.Activable;
import com.boulangerie.shared.model.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

@Entity
@Table(name = "categories_produit")
@Getter
@Setter
@Accessors(chain = true)
public class CategorieProduit extends AbstractAuditingEntity implements GenericEntity<CategorieProduit>, Activable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle", length = 100, nullable = false)
    private String libelle;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @Override
    public CategorieProduit createNewInstance() {
        return new CategorieProduit();
    }
}