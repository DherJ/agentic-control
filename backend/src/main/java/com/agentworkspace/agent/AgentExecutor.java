package com.agentworkspace.agent;
public interface AgentExecutor {
    AgentType supportedType();
    AgentExecutionResult execute(AgentExecutionContext context);
}
