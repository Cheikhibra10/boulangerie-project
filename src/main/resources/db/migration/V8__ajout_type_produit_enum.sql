ALTER TABLE produits
    ADD COLUMN type_produit VARCHAR(30);
UPDATE produits
SET type_produit = 'PAIN'
WHERE libelle IN (
               'GP_1KG',
               'PP_DEMI_KG'
    );

UPDATE produits
SET type_produit = 'PAIN'
WHERE libelle IN (
               'GP_1KG',
               'PP_DEMI_KG'
    );

