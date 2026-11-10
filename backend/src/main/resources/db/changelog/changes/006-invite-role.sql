--liquibase formatted sql
--changeset agent-workspace:006-invite-role
ALTER TABLE agent_workspace.app_user DROP CONSTRAINT IF EXISTS app_user_role_check;
ALTER TABLE agent_workspace.app_user ADD CONSTRAINT app_user_role_check CHECK (role IN ('ADMIN','USER','INVITE'));
