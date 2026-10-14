CREATE TABLE IF NOT EXISTS contrats (
                                        id              BIGSERIAL PRIMARY KEY,
                                        employe_id      BIGINT         NOT NULL REFERENCES employes(id),
                                        type            VARCHAR(20)    NOT NULL,
                                        date_debut      DATE           NOT NULL,
                                        date_fin        DATE,
                                        salaire_base    NUMERIC(15, 2) NOT NULL,
                                        poste           VARCHAR(100),
                                        statut          VARCHAR(20)    NOT NULL DEFAULT 'ACTIF',
                                        created_at      TIMESTAMPTZ,
                                        created_by      VARCHAR(100),
                                        updated_at      TIMESTAMPTZ,
                                        updated_by      VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_contrat_employe_id ON contrats(employe_id);
CREATE INDEX IF NOT EXISTS idx_contrat_statut ON contrats(statut);

CREATE TABLE IF NOT EXISTS bulletins_paie (
                                              id               BIGSERIAL PRIMARY KEY,
                                              employe_id       BIGINT         NOT NULL REFERENCES employes(id),
                                              contrat_id       BIGINT         NOT NULL REFERENCES contrats(id),
                                              periode_id       BIGINT         NOT NULL,
                                              salaire_brut     NUMERIC(15, 2) NOT NULL,
                                              retenues         NUMERIC(15, 2) NOT NULL DEFAULT 0,
                                              net_a_payer      NUMERIC(15, 2) NOT NULL,
                                              jours_travailles NUMERIC(5, 1),
                                              jours_absents    NUMERIC(5, 1)  NOT NULL DEFAULT 0,
                                              statut           VARCHAR(20)    NOT NULL DEFAULT 'BROUILLON',
                                              date_paiement    DATE,
                                              created_at       TIMESTAMPTZ,
                                              created_by       VARCHAR(100),
                                              updated_at       TIMESTAMPTZ,
                                              updated_by       VARCHAR(100),
                                              CONSTRAINT uk_bulletin_employe_periode UNIQUE (employe_id, periode_id)
);

CREATE INDEX IF NOT EXISTS idx_bulletin_employe_id ON bulletins_paie(employe_id);
CREATE INDEX IF NOT EXISTS idx_bulletin_periode_id ON bulletins_paie(periode_id);
CREATE INDEX IF NOT EXISTS idx_bulletin_statut ON bulletins_paie(statut);


CREATE TABLE IF NOT EXISTS presences (
                                         id             BIGSERIAL PRIMARY KEY,
                                         employe_id     BIGINT      NOT NULL REFERENCES employes(id),
                                         date           DATE        NOT NULL,
                                         statut         VARCHAR(20) NOT NULL DEFAULT 'PRESENT',
                                         heure_arrivee  TIME,
                                         heure_depart   TIME,
                                         created_at     TIMESTAMPTZ,
                                         created_by     VARCHAR(100),
                                         updated_at     TIMESTAMPTZ,
                                         updated_by     VARCHAR(100),
                                         CONSTRAINT uk_presence_employe_date UNIQUE (employe_id, date)
);

CREATE INDEX IF NOT EXISTS idx_presence_employe_id ON presences(employe_id);
CREATE INDEX IF NOT EXISTS idx_presence_date ON presences(date);
CREATE INDEX IF NOT EXISTS idx_presence_statut ON presences(statut);

CREATE TABLE IF NOT EXISTS avances_salaire (
                                               id            BIGSERIAL PRIMARY KEY,
                                               employe_id    BIGINT         NOT NULL REFERENCES employes(id),
                                               montant       NUMERIC(15, 2) NOT NULL,
                                               date_demande  DATE           NOT NULL,
                                               statut        VARCHAR(20)    NOT NULL DEFAULT 'EN_ATTENTE',
                                               bulletin_id   BIGINT,
                                               created_at    TIMESTAMPTZ,
                                               created_by    VARCHAR(100),
                                               updated_at    TIMESTAMPTZ,
                                               updated_by    VARCHAR(100)
);

CREATE INDEX IF NOT EXISTS idx_avance_employe_id ON avances_salaire(employe_id);
CREATE INDEX IF NOT EXISTS idx_avance_statut ON avances_salaire(statut);
CREATE INDEX IF NOT EXISTS idx_avance_date ON avances_salaire(date_demande);