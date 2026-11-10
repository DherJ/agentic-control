--liquibase formatted sql
--changeset agent-workspace:001
CREATE SCHEMA IF NOT EXISTS agent_workspace;

CREATE TABLE agent_workspace.app_user (
  id UUID PRIMARY KEY, username VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL, role VARCHAR(20) NOT NULL CHECK (role IN ('ADMIN','USER')),
  enabled BOOLEAN NOT NULL DEFAULT TRUE, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE agent_workspace.project (
  id UUID PRIMARY KEY, owner_id UUID NOT NULL REFERENCES agent_workspace.app_user(id),
  name VARCHAR(150) NOT NULL, description TEXT, github_repository VARCHAR(255),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE TABLE agent_workspace.project_member (
  project_id UUID NOT NULL REFERENCES agent_workspace.project(id),
  user_id UUID NOT NULL REFERENCES agent_workspace.app_user(id),
  membership_role VARCHAR(20) NOT NULL CHECK (membership_role IN ('OWNER','MEMBER')),
  PRIMARY KEY (project_id, user_id)
);
CREATE TABLE agent_workspace.workflow (
  id UUID PRIMARY KEY, project_id UUID NOT NULL REFERENCES agent_workspace.project(id),
  title VARCHAR(200) NOT NULL, raw_request TEXT NOT NULL,
  status VARCHAR(50) NOT NULL, resume_status VARCHAR(50), version INTEGER NOT NULL DEFAULT 0,
  created_by UUID NOT NULL REFERENCES agent_workspace.app_user(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(), updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  completed_at TIMESTAMPTZ
);
CREATE INDEX idx_workflow_project_status ON agent_workspace.workflow(project_id,status);
CREATE TABLE agent_workspace.agent_task (
  id UUID PRIMARY KEY, workflow_id UUID NOT NULL REFERENCES agent_workspace.workflow(id),
  agent_type VARCHAR(50) NOT NULL, status VARCHAR(30) NOT NULL,
  sequence_number INTEGER NOT NULL, created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  completed_at TIMESTAMPTZ
);
CREATE TABLE agent_workspace.agent_run (
  id UUID PRIMARY KEY, task_id UUID NOT NULL REFERENCES agent_workspace.agent_task(id),
  attempt INTEGER NOT NULL, status VARCHAR(30) NOT NULL,
  started_at TIMESTAMPTZ, finished_at TIMESTAMPTZ, error_message TEXT, metadata JSONB,
  UNIQUE (task_id,attempt)
);
CREATE TABLE agent_workspace.artifact (
  id UUID PRIMARY KEY, workflow_id UUID NOT NULL REFERENCES agent_workspace.workflow(id),
  task_id UUID REFERENCES agent_workspace.agent_task(id),
  type VARCHAR(50) NOT NULL, version INTEGER NOT NULL,
  content JSONB NOT NULL, content_hash VARCHAR(64) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  UNIQUE (workflow_id,type,version), UNIQUE (id,workflow_id)
);
CREATE TABLE agent_workspace.approval (
  id UUID PRIMARY KEY, workflow_id UUID NOT NULL REFERENCES agent_workspace.workflow(id),
  artifact_id UUID NOT NULL, type VARCHAR(30) NOT NULL,
  status VARCHAR(30) NOT NULL, requested_at TIMESTAMPTZ NOT NULL DEFAULT now(),
  decided_at TIMESTAMPTZ, decided_by UUID REFERENCES agent_workspace.app_user(id),
  comment TEXT, version INTEGER NOT NULL DEFAULT 0,
  FOREIGN KEY (artifact_id,workflow_id) REFERENCES agent_workspace.artifact(id,workflow_id)
);
CREATE UNIQUE INDEX uq_pending_approval ON agent_workspace.approval(workflow_id,type) WHERE status='PENDING';
CREATE TABLE agent_workspace.workflow_event (
  id UUID PRIMARY KEY, workflow_id UUID NOT NULL REFERENCES agent_workspace.workflow(id),
  event_type VARCHAR(80) NOT NULL, actor_id UUID REFERENCES agent_workspace.app_user(id),
  payload JSONB NOT NULL DEFAULT '{}'::jsonb, created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_workflow_event_order ON agent_workspace.workflow_event(workflow_id,created_at);
