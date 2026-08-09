package com.boulangerie.administration.model;

import com.boulangerie.shared.model.AbstractAuditingEntity;
import com.boulangerie.shared.model.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@Entity
@Table(name = "produits")
@Getter
@Setter
@Accessors(chain = true)
public class Produit extends AbstractAuditingEntity implements GenericEntity<Produit> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle", length = 100, nullable = false)
    private String libelle;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "image_public_id", length = 255)
    private String imagePublicId;

    @Column(name = "prix_detail", precision = 15, scale = 2, nullable = false)
    private BigDecimal prixDetail;

    @Column(name = "prix_gros", precision = 15, scale = 2)
    private BigDecimal prixGros;

    @Column(name = "prix_livreur", precision = 15, scale = 2)
    private BigDecimal prixLivreur;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_produit", nullable = false,length = 20)
    private TypeProduit typeProduit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categorie_id", nullable = false)
    private CategorieProduit categorie;

    @Column(name = "actif", nullable = false)
    private Boolean actif = true;

    @Override
    public Produit createNewInstance() {
        return new Produit();
    }

}