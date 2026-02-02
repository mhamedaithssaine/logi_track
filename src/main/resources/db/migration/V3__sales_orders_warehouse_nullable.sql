-- Client peut créer commande sans entrepôt ; le manager assigne l'entrepôt ensuite
ALTER TABLE sales_orders ALTER COLUMN warehouse_id DROP NOT NULL;
