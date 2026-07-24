// abonnements/model/ConsommationJournaliere.java
package com.boulangerie.abonnements.model;

import com.boulangerie.shared.model.AbstractAuditingEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;


@Entity
@Table(
        name = "consommations_journalieres",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_consommation_ligne_date",
                        columnNames = {
                                "ligne_id",
                                "date"
                        }
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ConsommationJournaliere extends AbstractAuditingEntity {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "ligne_id",
            nullable = false
    )
    private LigneAbonnement ligne;


    @Column(
            nullable = false
    )
    private LocalDate date;


    @Column(
            nullable = false,
            precision = 10,
            scale = 2
    )
    private BigDecimal quantite;



    private ConsommationJournaliere(
            LigneAbonnement ligne,
            LocalDate date,
            BigDecimal quantite
    ) {

        this.ligne = Objects.requireNonNull(ligne);
        this.date = Objects.requireNonNull(date);
        this.quantite = validateQuantite(quantite);
    }



    static ConsommationJournaliere creer(
            LigneAbonnement ligne,
            LocalDate date,
            BigDecimal quantite
    ) {

        return new ConsommationJournaliere(
                ligne,
                date,
                quantite
        );
    }



    public BigDecimal calculerMontant() {

        return ligne.calculerMontant(
                quantite
        );
    }



    private BigDecimal validateQuantite(
            BigDecimal quantite
    ) {

        if (quantite == null ||
                quantite.signum() <= 0) {

            throw new IllegalArgumentException(
                    "Quantité invalide"
            );
        }

        return quantite;
    }
}