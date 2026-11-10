--liquibase formatted sql
--changeset agent-workspace:004
ALTER TABLE agent_workspace.workflow ADD COLUMN workflow_definition_id UUID REFERENCES agent_workspace.workflow_definition(id);
CREATE INDEX idx_workflow_definition ON agent_workspace.workflow(workflow_definition_id);
CREATE TABLE agent_workspace.step_execution (
 id UUID PRIMARY KEY,
 workflow_id UUID NOT NULL REFERENCES agent_workspace.workflow(id),
 step_definition_id UUID NOT NULL REFERENCES agent_workspace.step_definition(id),
 iteration_number INTEGER NOT NULL DEFAULT 1 CHECK (iteration_number > 0),
 status VARCHAR(30) NOT NULL CHECK (status IN ('PENDING','READY','RUNNING','WAITING_APPROVAL','COMPLETED','REJECTED','FAILED','SKIPPED','CANCELLED')),
 started_at TIMESTAMPTZ,
 finished_at TIMESTAMPTZ,
 version INTEGER NOT NULL DEFAULT 0,
 CONSTRAINT uq_step_iteration UNIQUE(workflow_id,step_definition_id,iteration_number)
);
CREATE INDEX idx_step_execution_workflow ON agent_workspace.step_execution(workflow_id);
