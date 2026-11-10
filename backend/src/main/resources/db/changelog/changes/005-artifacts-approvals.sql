--liquibase formatted sql
--changeset agent-workspace:005
ALTER TABLE agent_workspace.artifact ADD COLUMN step_execution_id UUID REFERENCES agent_workspace.step_execution(id);
ALTER TABLE agent_workspace.approval ADD COLUMN step_execution_id UUID REFERENCES agent_workspace.step_execution(id);
CREATE INDEX idx_artifact_step_execution ON agent_workspace.artifact(step_execution_id);
CREATE INDEX idx_approval_step_execution ON agent_workspace.approval(step_execution_id);
-- Note: existing artifact and approval tables from 001 are reused; no destructive migrations.
