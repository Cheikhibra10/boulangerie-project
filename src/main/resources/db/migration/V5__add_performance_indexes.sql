-- ============================================================================
-- V5__add_performance_indexes.sql
-- Performance optimization indexes for search and filtering operations
-- ============================================================================

-- =============================================================================
-- ACHATS MODULE - Purchase management indexes
-- =============================================================================

-- Achat: Search by supplier (common filter on purchases page)
CREATE INDEX IF NOT EXISTS idx_achats_fournisseur_id
    ON achats(fournisseur_id);

-- Achat: Filter by status (EN_ATTENTE, COMPLETE, etc.)
CREATE INDEX IF NOT EXISTS idx_achats_statut_reception
    ON achats(statut_reception);

-- Achat: Filter by payment status
CREATE INDEX IF NOT EXISTS idx_achats_statut_paiement
    ON achats(statut_paiement);

-- Achat: Filter active vs cancelled purchases
CREATE INDEX IF NOT EXISTS idx_achats_statut_achat
    ON achats(statut_achat);

-- Achat: Date range queries for reporting
CREATE INDEX IF NOT EXISTS idx_achats_created_at
    ON achats(created_at);

-- LigneAchat: Fetch all lines for a purchase (very frequent)
CREATE INDEX IF NOT EXISTS idx_ligne_achat_achat_id
    ON lignes_achat(achat_id);

-- LigneAchat: Ingredient lookup for stock movements
CREATE INDEX IF NOT EXISTS idx_ligne_achat_ingredient_id
    ON lignes_achat(ingredient_id);

-- PaiementFournisseur: Lookup payments by purchase
CREATE INDEX IF NOT EXISTS idx_paiement_fournisseur_achat_id
    ON public.paiements_fournisseurs(achat_id);


-- =============================================================================
-- ABONNEMENTS MODULE - Subscription management indexes
-- =============================================================================

-- Abonnement: Filter active subscriptions (very frequent)
CREATE INDEX IF NOT EXISTS idx_abonnements_actif
    ON abonnements(actif)
    WHERE actif = true;

-- Abonnement: Filter by delivery person
CREATE INDEX IF NOT EXISTS idx_abonnements_livreur_id
    ON abonnements(livreur_id);

-- Abonnement: Date range queries for active subscriptions
CREATE INDEX IF NOT EXISTS idx_abonnements_date_debut
    ON abonnements(date_debut);

CREATE INDEX IF NOT EXISTS idx_abonnements_date_fin
    ON abonnements(date_fin);

-- Composite index for date range queries on active subscriptions
CREATE INDEX IF NOT EXISTS idx_abonnements_dates_actif
    ON abonnements(date_debut, date_fin, actif);

-- LigneAbonnement: Fetch subscription lines
CREATE INDEX IF NOT EXISTS idx_ligne_abonnement_abonnement_id
    ON lignes_abonnement(abonnement_id);

-- LigneAbonnement: Lookup by client
CREATE INDEX IF NOT EXISTS idx_ligne_abonnement_client_id
    ON lignes_abonnement(client_id);

-- ConsommationJournaliere: Date range queries for daily consumption
CREATE INDEX IF NOT EXISTS idx_consommation_journaliere_date
    ON consommations_journalieres(date);

-- ConsommationJournaliere: Lookup by subscription line
CREATE INDEX IF NOT EXISTS idx_consommation_journaliere_ligne_id
    ON consommations_journalieres(ligne_id);

-- CompteAbonnement: Lookup account by subscription (1-to-1)
CREATE INDEX IF NOT EXISTS idx_compte_abonnement_abonnement_id
    ON comptes_abonnement(abonnement_id);

-- PaiementAbonnement: Date range for payments reporting
CREATE INDEX IF NOT EXISTS idx_paiement_abonnement_created_at
    ON paiements_abonnement(created_at);

-- PaiementAbonnement: Filter by subscription
CREATE INDEX IF NOT EXISTS idx_paiement_abonnement_abonnement_id
    ON paiements_abonnement(ligne_abonnement_id);


-- =============================================================================
-- ADMINISTRATION MODULE - Master data indexes
-- =============================================================================

-- Produit: Filter active products (very frequent in sales/production)
CREATE INDEX IF NOT EXISTS idx_produits_actif
    ON produits(actif)
    WHERE actif = true;

-- Produit: Filter by category
CREATE INDEX IF NOT EXISTS idx_produits_categorie_id
    ON produits(categorie_id);

-- Produit: Filter by product type (PAIN, VIENNOISERIE, etc.)
CREATE INDEX IF NOT EXISTS idx_produits_type_produit
    ON produits(type_produit);

-- Produit: Composite index for active products by category
CREATE INDEX IF NOT EXISTS idx_produits_actif_categorie
    ON produits(actif, categorie_id)
    WHERE actif = true;

-- Ingredient: Filter active ingredients
CREATE INDEX IF NOT EXISTS idx_ingredients_actif
    ON ingredients(actif)
    WHERE actif = true;

-- Recette: Lookup recipes by product
CREATE INDEX IF NOT EXISTS idx_recettes_produit_id
    ON recettes(produit_id);

-- Recette: Filter active recipes
CREATE INDEX IF NOT EXISTS idx_recettes_actif
    ON recettes(actif)
    WHERE actif = true;

-- Recette: Composite index for active recipes by product
CREATE INDEX IF NOT EXISTS idx_recettes_produit_actif
    ON recettes(produit_id, actif)
    WHERE actif = true;

-- RecetteIngredient: Lookup ingredients for a recipe
CREATE INDEX IF NOT EXISTS idx_recette_ingredient_recette_id
    ON recette_ingredients(recette_id);

-- RecetteIngredient: Lookup recipes using an ingredient
CREATE INDEX IF NOT EXISTS idx_recette_ingredient_ingredient_id
    ON recette_ingredients(ingredient_id);

-- Fournisseur: Filter active suppliers
-- Note: 'actif' column is in parent 'personnes' table due to JOINED inheritance
-- Index on personnes table will be used for filtering fournisseurs

-- Livreur: Filter active delivery persons
-- Note: 'actif' column is in parent 'personnes' table due to JOINED inheritance
-- Index on personnes table will be used for filtering livreurs

-- Client: Filter active clients
CREATE INDEX IF NOT EXISTS idx_clients_actif
    ON clients(actif)
    WHERE actif = true;

-- CategorieProduit: Filter active categories
CREATE INDEX IF NOT EXISTS idx_categorie_produit_actif
    ON categories_produit(actif)
    WHERE actif = true;

-- CategorieDepense: Filter active expense categories
CREATE INDEX IF NOT EXISTS idx_categorie_depense_actif
    ON categories_depense(actif)
    WHERE actif = true;


-- =============================================================================
-- COMPTABILITE MODULE - Accounting and cash management indexes
-- =============================================================================

-- Caisse: Lookup by status (OUVERTE, FERMEE)
CREATE INDEX IF NOT EXISTS idx_caisses_statut
    ON caisses(statut);

-- Caisse: Date range queries
CREATE INDEX IF NOT EXISTS idx_caisses_date_ouverture
    ON caisses(date_ouverture);

-- MouvementCaisse: Filter by cash register
CREATE INDEX IF NOT EXISTS idx_mouvement_caisse_caisse_id
    ON mouvements_caisse(caisse_id);

-- MouvementCaisse: Filter by movement type
CREATE INDEX IF NOT EXISTS idx_mouvement_caisse_type_mouvement
    ON mouvements_caisse(type_mouvement);

-- MouvementCaisse: Filter by direction (ENTREE, SORTIE)
CREATE INDEX IF NOT EXISTS idx_mouvement_caisse_sens
    ON mouvements_caisse(sens);

-- MouvementCaisse: Date range queries for cash movements
CREATE INDEX IF NOT EXISTS idx_mouvement_caisse_created_at
    ON mouvements_caisse(created_at);

-- MouvementCaisse: Composite index for filtering by cash register and date
CREATE INDEX IF NOT EXISTS idx_mouvement_caisse_caisse_date
    ON mouvements_caisse(caisse_id, created_at);

-- DepensePeriode: Lookup expenses by period
CREATE INDEX IF NOT EXISTS idx_depense_periode_periode_id
    ON depenses_periode(periode_id);

-- DepensePeriode: Filter by category
CREATE INDEX IF NOT EXISTS idx_depense_periode_categorie_id
    ON depenses_periode(categorie_id);

-- ResultatPeriode: Lookup result by period (1-to-1)
CREATE INDEX IF NOT EXISTS idx_resultat_periode_periode_id
    ON resultat_periode(periode_id);


-- =============================================================================
-- LIVREURS MODULE - Delivery person accounts indexes
-- =============================================================================

-- CompteLivreur: Lookup account by delivery person (1-to-1)
CREATE INDEX IF NOT EXISTS idx_compte_livreur_livreur_id
    ON comptes_livreur(livreur_id);

-- CompteLivreurJournalier: Lookup daily accounts by delivery person
CREATE INDEX IF NOT EXISTS idx_compte_livreur_journalier_livreur_id
    ON comptes_livreur_journalier(livreur_id);

-- CompteLivreurJournalier: Date range queries
CREATE INDEX IF NOT EXISTS idx_compte_livreur_journalier_date
    ON comptes_livreur_journalier(date);

-- CompteLivreurJournalier: Composite for delivery person + date range
CREATE INDEX IF NOT EXISTS idx_compte_livreur_journalier_livreur_date
    ON comptes_livreur_journalier(livreur_id, date);

-- CompteLivreurJournalier: Filter by status
CREATE INDEX IF NOT EXISTS idx_compte_livreur_journalier_statut
    ON comptes_livreur_journalier(statut);

-- LigneCompteLivreur: Lookup lines by daily account
CREATE INDEX IF NOT EXISTS idx_ligne_compte_livreur_journalier_id
    ON lignes_compte_livreur(journalier_id);

-- CommissionRegle: Date range queries
CREATE INDEX IF NOT EXISTS idx_commission_regle_date
    ON commissions_regle(date_debut);

-- CommissionRegle: Filter by delivery person
CREATE INDEX IF NOT EXISTS idx_commission_regle_livreur_id
    ON commissions_regle(livreur_id);

-- VersementLivreur: Lookup by daily account (1-to-1)
CREATE INDEX IF NOT EXISTS idx_versement_livreur_compte_rendu_id
    ON versement_livreurs(journalier_id);


-- =============================================================================
-- PRODUCTION MODULE - Production management indexes
-- =============================================================================

-- LotProduction: Date range queries (very frequent for daily production)
CREATE INDEX IF NOT EXISTS idx_lot_production_date
    ON lots_production(date);

-- LotProduction: Filter by product
CREATE INDEX IF NOT EXISTS idx_lot_production_produit_id
    ON lots_production(produit_id);

-- LotProduction: Filter by status
CREATE INDEX IF NOT EXISTS idx_lot_production_statut
    ON lots_production(statut);

-- LotProduction: Composite for product + date queries
CREATE INDEX IF NOT EXISTS idx_lot_production_produit_date
    ON lots_production(produit_id, date);

-- DestinationProduction: Lookup by production lot
CREATE INDEX IF NOT EXISTS idx_destination_production_lot_id
    ON destinations_production(lot_id);

-- DestinationProduction: Date range queries
CREATE INDEX IF NOT EXISTS idx_destination_production_date
    ON destinations_production(date);

-- DestinationProduction: Filter by distribution channel
CREATE INDEX IF NOT EXISTS idx_destination_production_canal
    ON destinations_production(canal);

-- DestinationProduction: Filter by delivery person
CREATE INDEX IF NOT EXISTS idx_destination_production_livreur_id
    ON destinations_production(livreur_id);

-- DestinationProduction: Filter by bread state
CREATE INDEX IF NOT EXISTS idx_destination_production_etat_pain
    ON destinations_production(etat_pain);

-- DestinationProduction: Composite for date + channel queries
CREATE INDEX IF NOT EXISTS idx_destination_production_date_canal
    ON destinations_production(date, canal);


-- =============================================================================
-- STOCKS MODULE - Inventory management indexes
-- =============================================================================

-- StockIngredient: Lookup by ingredient (1-to-1)
CREATE INDEX IF NOT EXISTS idx_stock_ingredient_ingredient_id
    ON stock_ingredients(ingredient_id);

-- StockProduit: Lookup by product (1-to-1)
CREATE INDEX IF NOT EXISTS idx_stock_produit_produit_id
    ON stock_produit(produit_id);

-- MouvementStock: Filter by ingredient
CREATE INDEX IF NOT EXISTS idx_mouvement_stock_ingredient_id
    ON mouvements_stock(ingredient_id);

-- MouvementStock: Filter by movement type
CREATE INDEX IF NOT EXISTS idx_mouvement_stock_type
    ON mouvements_stock(type);

-- MouvementStock: Date range queries
CREATE INDEX IF NOT EXISTS idx_mouvement_stock_date
    ON mouvements_stock(date);

-- MouvementStock: Filter by status
CREATE INDEX IF NOT EXISTS idx_mouvement_stock_statut
    ON mouvements_stock(statut);

-- MouvementStock: Lookup by production lot
CREATE INDEX IF NOT EXISTS idx_mouvement_stock_lot_production_id
    ON mouvements_stock(lot_production_id)
    WHERE lot_production_id IS NOT NULL;

-- MouvementStock: Lookup by purchase line
CREATE INDEX IF NOT EXISTS idx_mouvement_stock_ligne_achat_id
    ON mouvements_stock(ligne_achat_id)
    WHERE ligne_achat_id IS NOT NULL;

-- MouvementStock: Composite for ingredient + date queries
CREATE INDEX IF NOT EXISTS idx_mouvement_stock_ingredient_date
    ON mouvements_stock(ingredient_id, date);

-- StockIngredientSnapshot: Lookup by ingredient
CREATE INDEX IF NOT EXISTS idx_stock_ingredient_snapshot_ingredient_id
    ON stock_ingredient_snapshots(ingredient_id);

-- StockIngredientSnapshot: Lookup by period
CREATE INDEX IF NOT EXISTS idx_stock_ingredient_snapshot_periode_id
    ON stock_ingredient_snapshots(periode_id);

-- StockInitial: Lookup by ingredient
CREATE INDEX IF NOT EXISTS idx_stock_initial_ingredient_id
    ON stock_initial(ingredient_id);

-- Personnes: Filter active persons (covers Fournisseur and Livreur via JOINED inheritance)
CREATE INDEX IF NOT EXISTS idx_personnes_actif
    ON personnes(actif)
    WHERE actif = true;


-- =============================================================================
-- VENTES MODULE - Sales management indexes
-- =============================================================================

-- VenteBoutique: Filter by user
CREATE INDEX IF NOT EXISTS idx_vente_boutique_utilisateur_id
    ON ventes_boutique(utilisateur_id);

-- VenteBoutique: Filter by status
CREATE INDEX IF NOT EXISTS idx_vente_boutique_statut
    ON ventes_boutique(statut);

-- LigneVenteBoutique: Lookup by sale
CREATE INDEX IF NOT EXISTS idx_ligne_vente_boutique_vente_id
    ON lignes_vente_boutique(vente_id);

-- LigneVenteBoutique: Lookup by product
CREATE INDEX IF NOT EXISTS idx_ligne_vente_boutique_produit_id
    ON lignes_vente_boutique(produit_id);

-- Paiement: Lookup by sale (1-to-1)
CREATE INDEX IF NOT EXISTS idx_paiement_vente_id
    ON paiements(vente_id);


-- =============================================================================
-- SHARED/AUDIT - Cross-cutting concerns indexes
-- =============================================================================

-- NOTE: audit_logs indexes already exist in V3__create_audit_log.sql:
-- - idx_audit_logs_entity (entity_name, entity_id)
-- - idx_audit_logs_performed_at (performed_at)

-- Additional audit index for filtering by action type
CREATE INDEX IF NOT EXISTS idx_audit_logs_action
    ON audit_logs(action);

-- Composite index for entity + performed_at queries
CREATE INDEX IF NOT EXISTS idx_audit_logs_entity_date
    ON audit_logs(entity_name, entity_id, performed_at);


-- =============================================================================
-- NOTIFICATIONS MODULE - Notification system indexes
-- =============================================================================




-- =============================================================================
-- PERFORMANCE NOTES
-- =============================================================================
--
-- 1. Partial indexes (WHERE clauses) are used for boolean filters like 'actif'
--    to reduce index size and improve performance on filtered queries.
--
-- 2. Composite indexes are created for common query patterns that filter on
--    multiple columns (e.g., date ranges + entity ID).
--
-- 3. Foreign key indexes are explicitly created as PostgreSQL doesn't
--    automatically index foreign key columns.
--
-- 4. Date/timestamp indexes support common reporting and date-range queries.
--
-- 5. All indexes use IF NOT EXISTS to support safe re-runs and migrations.
--
-- =============================================================================
