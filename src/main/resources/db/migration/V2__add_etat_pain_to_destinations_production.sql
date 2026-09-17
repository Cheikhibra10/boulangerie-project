ALTER TABLE destinations_production
    ADD COLUMN etat_pain VARCHAR(255);

UPDATE destinations_production
SET etat_pain = 'FRAIS'
WHERE etat_pain IS NULL;