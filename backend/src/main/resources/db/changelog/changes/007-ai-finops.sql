--liquibase formatted sql
--changeset agent-workspace:007-ai-finops
CREATE TABLE agent_workspace.ai_project_budget (
 project_id UUID PRIMARY KEY REFERENCES agent_workspace.project(id) ON DELETE CASCADE,
 budget_usd NUMERIC(16,6) NOT NULL DEFAULT 10 CHECK (budget_usd > 0),
 request_limit_usd NUMERIC(16,6) NOT NULL DEFAULT 2 CHECK (request_limit_usd > 0),
 alert_percent INTEGER NOT NULL DEFAULT 80 CHECK (alert_percent BETWEEN 1 AND 100),
 reserved_usd NUMERIC(16,6) NOT NULL DEFAULT 0 CHECK (reserved_usd >= 0),
 updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
CREATE TABLE agent_workspace.ai_usage (
 id UUID PRIMARY KEY,
 project_id UUID NOT NULL REFERENCES agent_workspace.project(id) ON DELETE CASCADE,
 workflow_id UUID NOT NULL REFERENCES agent_workspace.workflow(id) ON DELETE CASCADE,
 step_id UUID NOT NULL,
 request_text TEXT NOT NULL,
 agent_type VARCHAR(64) NOT NULL,
 model VARCHAR(128) NOT NULL,
 input_tokens BIGINT NOT NULL DEFAULT 0,
 cached_input_tokens BIGINT NOT NULL DEFAULT 0,
 output_tokens BIGINT NOT NULL DEFAULT 0,
 input_usd_per_million NUMERIC(16,6) NOT NULL,
 output_usd_per_million NUMERIC(16,6) NOT NULL,
 cost_usd NUMERIC(16,6) NOT NULL DEFAULT 0,
 reserved_usd NUMERIC(16,6) NOT NULL DEFAULT 0,
 status VARCHAR(24) NOT NULL CHECK (status IN ('RESERVED','COMPLETED','FAILED')),
 error_message TEXT,
 created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
 completed_at TIMESTAMPTZ
);
CREATE INDEX idx_ai_usage_project_created ON agent_workspace.ai_usage(project_id, created_at DESC);
CREATE INDEX idx_ai_usage_project_cost ON agent_workspace.ai_usage(project_id, cost_usd DESC);
CREATE UNIQUE INDEX uq_ai_usage_step_reserved ON agent_workspace.ai_usage(step_id) WHERE status = 'RESERVED';
