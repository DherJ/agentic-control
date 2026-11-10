--liquibase formatted sql
--changeset agent-workspace:002
CREATE INDEX IF NOT EXISTS idx_project_member_user ON agent_workspace.project_member(user_id);
CREATE INDEX IF NOT EXISTS idx_approval_workflow_status ON agent_workspace.approval(workflow_id,status);
CREATE INDEX IF NOT EXISTS idx_agent_task_workflow ON agent_workspace.agent_task(workflow_id);
