-- V1__initial_schema.sql
-- Compatible with both H2 (PostgreSQL mode) and PostgreSQL.

CREATE TABLE providers (
    id              UUID            PRIMARY KEY,
    npi             VARCHAR(10)     NOT NULL UNIQUE,
    name            VARCHAR(200)    NOT NULL,
    region          VARCHAR(64),
    specialty       VARCHAR(128),
    active          BOOLEAN         NOT NULL,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP       NOT NULL
);

CREATE INDEX idx_provider_region ON providers (region);
CREATE INDEX idx_provider_active ON providers (active);

CREATE TABLE routing_rules (
    id              UUID            PRIMARY KEY,
    name            VARCHAR(128)    NOT NULL,
    claim_type      VARCHAR(32),
    min_amount      DECIMAL(12, 2),
    max_amount      DECIMAL(12, 2),
    region          VARCHAR(64),
    destination     VARCHAR(128)    NOT NULL,
    priority        INTEGER         NOT NULL,
    active          BOOLEAN         NOT NULL,
    created_at      TIMESTAMP       NOT NULL,
    updated_at      TIMESTAMP       NOT NULL
);

CREATE INDEX idx_rule_priority ON routing_rules (priority);
CREATE INDEX idx_rule_active ON routing_rules (active);

CREATE TABLE claims (
    id                  UUID            PRIMARY KEY,
    claim_number        VARCHAR(64)     NOT NULL UNIQUE,
    patient_id          VARCHAR(64)     NOT NULL,
    provider_id         UUID            NOT NULL,
    claim_type          VARCHAR(32)     NOT NULL,
    amount              DECIMAL(12, 2)  NOT NULL,
    service_date        DATE            NOT NULL,
    submitted_at        TIMESTAMP       NOT NULL,
    status              VARCHAR(32)     NOT NULL,
    routing_destination VARCHAR(128),
    raw_payload         TEXT            NOT NULL,
    rejection_reason    VARCHAR(512),
    created_at          TIMESTAMP       NOT NULL,
    updated_at          TIMESTAMP       NOT NULL,
    CONSTRAINT fk_claim_provider FOREIGN KEY (provider_id) REFERENCES providers (id),
    CONSTRAINT chk_claim_amount_positive CHECK (amount > 0)
);

CREATE INDEX idx_claim_patient ON claims (patient_id);
CREATE INDEX idx_claim_provider ON claims (provider_id);
CREATE INDEX idx_claim_status ON claims (status);
CREATE INDEX idx_claim_status_type ON claims (status, claim_type);

CREATE TABLE routing_decisions (
    id              UUID            PRIMARY KEY,
    claim_id        UUID            NOT NULL,
    matched_rule_id UUID,
    destination     VARCHAR(128),
    decision_at     TIMESTAMP       NOT NULL,
    outcome         VARCHAR(16)     NOT NULL,
    notes           VARCHAR(1024),
    created_at      TIMESTAMP       NOT NULL,
    CONSTRAINT fk_decision_claim FOREIGN KEY (claim_id) REFERENCES claims (id),
    CONSTRAINT fk_decision_rule  FOREIGN KEY (matched_rule_id) REFERENCES routing_rules (id)
);

CREATE INDEX idx_decision_claim ON routing_decisions (claim_id);
CREATE INDEX idx_decision_rule ON routing_decisions (matched_rule_id);
CREATE INDEX idx_decision_claim_time ON routing_decisions (claim_id, decision_at);
