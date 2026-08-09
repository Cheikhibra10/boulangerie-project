ALTER TABLE mouvements_caisse
    ADD CONSTRAINT mouvements_caisse_type_mouvement_check
        CHECK (
            type_mouvement IN (
                               'PAIEMENT',
                               'REMBOURSEMENT_VENTE',
                               'COMPLEMENT_PAIEMENT_VENTE',
                               'PAIEMENT_ABONNEMENT',
                               'VERSEMENT_LIVREUR',
                               'PAIEMENT_FOURNISSEUR',
                               'DEPENSE_PERIODE'
                )
            );

ALTER TABLE ventes_boutique
    ADD COLUMN motifAnnulation VARCHAR(255);