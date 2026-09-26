-- Verrouillage optimiste sur les entités où une écriture concurrente
-- perdue est un risque métier réel (survente de stock, double fermeture
-- de caisse/période) — voir les commentaires sur les champs "version"
-- correspondants dans StockProduit, StockIngredient, Caisse et Periode.
--
-- DEFAULT 0 + backfill implicite : les lignes existantes reçoivent 0,
-- cohérent avec ce que Hibernate attend comme version initiale.

ALTER TABLE stock_produit
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE stock_ingredients
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE caisses
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;

ALTER TABLE periodes
    ADD COLUMN version BIGINT NOT NULL DEFAULT 0;