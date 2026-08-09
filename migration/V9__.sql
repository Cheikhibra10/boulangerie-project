
ALTER TABLE produits
DROP
COLUMN image;

ALTER TABLE produits
    ADD image_public_id VARCHAR(255);

ALTER TABLE produits
    ADD image_url TEXT;


