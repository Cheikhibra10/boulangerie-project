CREATE TABLE audit_logs
(
    id           BIGSERIAL PRIMARY KEY,
    entity_name  VARCHAR(150) NOT NULL,
    entity_id    BIGINT,
    action       VARCHAR(20)  NOT NULL,
    performed_by VARCHAR(255),
    performed_at TIMESTAMP    NOT NULL,
    snapshot     TEXT
);

CREATE INDEX idx_audit_logs_entity ON audit_logs (entity_name, entity_id);
CREATE INDEX idx_audit_logs_performed_at ON audit_logs (performed_at);