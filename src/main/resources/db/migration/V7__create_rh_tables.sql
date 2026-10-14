CREATE TABLE IF NOT EXISTS employes (
                                        id              BIGSERIAL PRIMARY KEY,
                                        matricule       VARCHAR(30)  NOT NULL UNIQUE,
                                        nom             VARCHAR(100) NOT NULL,
                                        prenom          VARCHAR(100) NOT NULL,
                                        telephone       VARCHAR(30),
                                        email           VARCHAR(150),
                                        date_embauche   DATE         NOT NULL,
                                        date_sortie     DATE,
                                        statut          VARCHAR(20)  NOT NULL DEFAULT 'ACTIF',
                                        utilisateur_id  BIGINT,
                                        livreur_id      BIGINT,
                                        created_at      TIMESTAMPTZ,
                                        created_by      VARCHAR(100),
                                        updated_at      TIMESTAMPTZ,
                                        updated_by      VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_employe_statut ON employes(statut);
CREATE INDEX IF NOT EXISTS idx_employe_nom ON employes(nom);

CREATE TABLE IF NOT EXISTS absences (
                                        id              BIGSERIAL PRIMARY KEY,
                                        employe_id      BIGINT       NOT NULL REFERENCES employes(id),
                                        type            VARCHAR(30)  NOT NULL,
                                        date_debut      DATE         NOT NULL,
                                        date_fin        DATE         NOT NULL,
                                        statut          VARCHAR(20)  NOT NULL DEFAULT 'EN_ATTENTE',
                                        motif           VARCHAR(500),
                                        created_at      TIMESTAMPTZ,
                                        created_by      VARCHAR(100),
                                        updated_at      TIMESTAMPTZ,
                                        updated_by      VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_absence_employe_id ON absences(employe_id);
CREATE INDEX IF NOT EXISTS idx_absence_statut ON absences(statut);
CREATE INDEX IF NOT EXISTS idx_absence_type ON absences(type);
CREATE INDEX IF NOT EXISTS idx_absence_dates ON absences(date_debut, date_fin);