
ALTER TABLE ventes_boutique
 DROP COLUMN motif_annulation;

ALTER TABLE ventes_boutique
    ADD COLUMN motif_annulation VARCHAR(255);
