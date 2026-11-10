--liquibase formatted sql
--changeset agent-workspace:008-global-monthly-finops
CREATE TABLE agent_workspace.ai_billing_settings (
 id INTEGER PRIMARY KEY CHECK (id = 1),
 monthly_budget_usd NUMERIC(16,6) NOT NULL DEFAULT 50 CHECK (monthly_budget_usd > 0),
 alert_percent INTEGER NOT NULL DEFAULT 80 CHECK (alert_percent BETWEEN 1 AND 100),
 enforce_limit BOOLEAN NOT NULL DEFAULT TRUE,
 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
INSERT INTO agent_workspace.ai_billing_settings(id) VALUES (1);
CREATE INDEX idx_ai_usage_created ON agent_workspace.ai_usage(created_at);
