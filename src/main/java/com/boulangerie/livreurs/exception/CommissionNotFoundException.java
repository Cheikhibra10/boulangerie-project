package com.boulangerie.livreurs.exception;

import java.time.LocalDate;

public class CommissionNotFoundException extends RuntimeException {

    public CommissionNotFoundException(
            Long livreurId,
            Long produitId,
            LocalDate date
    ) {
        super("""
              Aucune règle de commission trouvée.
              Livreur=%d
              Produit=%d
              Date=%s
              """.formatted(livreurId, produitId, date));
    }
}