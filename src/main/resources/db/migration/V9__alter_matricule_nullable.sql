ALTER TABLE employes
    ALTER COLUMN matricule DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_employes_matricule
    ON employes (matricule)
    WHERE matricule IS NOT NULL;