
ALTER TABLE ventes_boutique
 DROP COLUMN motifannulation;

ALTER TABLE ventes_boutique
    ADD COLUMN motif_annulation VARCHAR(255);
