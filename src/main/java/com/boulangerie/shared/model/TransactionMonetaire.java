package com.boulangerie.shared.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.math.BigDecimal;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public abstract class TransactionMonetaire extends AbstractAuditingEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "montant", precision = 15, scale = 2, nullable = false)
    private BigDecimal montant;

    @Enumerated(EnumType.STRING)
    @Column(name = "type_paiement", nullable = false)
    private TypePaiement modePaiement;

    @Column(name = "libelle")
    private String libelle;

    protected TransactionMonetaire(
            BigDecimal montant,
            TypePaiement modePaiement,
            String libelle
    ) {
        if (montant == null || montant.signum() <= 0) {
            throw new IllegalArgumentException("Montant invalide");
        }

        this.montant = montant;
        this.modePaiement = modePaiement;
        this.libelle = libelle;
    }
}