ALTER TABLE mouvements_caisse
DROP CONSTRAINT mouvements_caisse_type_mouvement_check;

ALTER TABLE mouvements_caisse
    ADD CONSTRAINT mouvements_caisse_type_mouvement_check
        CHECK (
            type_mouvement IN (
                               'PAIEMENT',
                               'PAIEMENT_ABONNEMENT',
                               'VERSEMENT_LIVREUR',
                               'PAIEMENT_FOURNISSEUR',
                               'DEPENSE_PERIODE',
                               'REPORT_BENEFICE',
                               'VERSEMENT_ABONNEMENT',
                               'REMBOURSEMENT_VENTE',
                               'COMPLEMENT_PAIEMENT_VENTE',
                               'REVERSEMENT_ABONNEMENT'

                )
            );