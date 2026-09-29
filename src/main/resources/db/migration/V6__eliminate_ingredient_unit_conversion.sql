-- Élimine le mécanisme de conversion d'unité (Ingredient.equivalenceStock)
-- pour TOUT ingrédient acheté en unité groupée (sac, carton...) — le stock
-- est désormais tracké directement dans l'unité d'achat de l'ingrédient
-- (Ingredient.unite), plus de conversion vers une unité de base implicite.
--
-- ORDRE CRITIQUE : on convertit d'abord les données existantes en utilisant
-- equivalence_stock, PUIS seulement on supprime la colonne. L'inverser
-- perdrait le facteur de conversion avant d'avoir pu l'appliquer.
--
-- Deux tables portent des quantités dans l'ancienne unité de base et
-- doivent être reconverties — pas seulement les recettes : les quantités
-- de stock DÉJÀ EN BASE (achats déjà réceptionnés) seraient sinon
-- silencieusement mal interprétées après cette migration (ex: "300" qui
-- voulait dire "300 kg" se lirait soudain comme "300 sacs").
--
-- valeur_totale n'a PAS besoin d'être reconvertie : une valeur monétaire
-- ne change pas de sens quand on change l'unité de la quantité qu'elle
-- représente.

-- --- Stock déjà en base (quantite, seuil_alerte) ---
UPDATE stock_ingredients si
SET
    quantite     = si.quantite / i.equivalence_stock,
    seuil_alerte = si.seuil_alerte / i.equivalence_stock
FROM ingredients i
WHERE si.ingredient_id = i.id
  AND i.equivalence_stock IS NOT NULL
  AND i.equivalence_stock <> 0
  AND i.equivalence_stock <> 1;

-- --- Quantités de recette (RecetteIngredient.quantite) ---
UPDATE recette_ingredients ri
SET quantite = ri.quantite / i.equivalence_stock
FROM ingredients i
WHERE ri.ingredient_id = i.id
  AND i.equivalence_stock IS NOT NULL
  AND i.equivalence_stock <> 0
  AND i.equivalence_stock <> 1;

-- --- Suppression du mécanisme lui-même ---
ALTER TABLE ingredients
    DROP COLUMN equivalence_stock;