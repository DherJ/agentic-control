package com.agentworkspace.agent;
import java.util.Map;
import java.util.UUID;
public record AgentExecutionContext(UUID workflowId, UUID taskId, AgentType agentType, Map<String,Object> inputs) {}
