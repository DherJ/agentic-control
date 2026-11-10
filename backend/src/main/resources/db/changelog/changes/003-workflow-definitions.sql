--liquibase formatted sql
--changeset agent-workspace:003
CREATE TABLE agent_workspace.workflow_definition (
  id UUID PRIMARY KEY,
  definition_key VARCHAR(100) NOT NULL,
  definition_version VARCHAR(50) NOT NULL,
  name VARCHAR(200) NOT NULL,
  definition_json JSONB NOT NULL,
  checksum VARCHAR(64) NOT NULL,
  published_at TIMESTAMPTZ NOT NULL,
  CONSTRAINT uq_workflow_definition_key_version UNIQUE (definition_key, definition_version)
);
CREATE TABLE agent_workspace.step_definition (
  id UUID PRIMARY KEY,
  workflow_definition_id UUID NOT NULL REFERENCES agent_workspace.workflow_definition(id) ON DELETE RESTRICT,
  step_key VARCHAR(100) NOT NULL,
  phase_key VARCHAR(100) NOT NULL,
  kind VARCHAR(30) NOT NULL CHECK (kind IN ('AGENT','APPROVAL','HANDOFF')),
  config_json JSONB NOT NULL,
  CONSTRAINT uq_step_definition_key UNIQUE (workflow_definition_id,step_key)
);
CREATE INDEX idx_step_definition_workflow ON agent_workspace.step_definition(workflow_definition_id);
