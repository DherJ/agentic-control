package com.agentworkspace.agent;
import java.util.Map;
public record AgentExecutionResult(boolean success,String artifactType,Map<String,Object> output,String errorMessage,long inputTokens,long cachedInputTokens,long outputTokens) {
 public AgentExecutionResult(boolean success,String artifactType,Map<String,Object> output,String errorMessage){this(success,artifactType,output,errorMessage,0,0,0);}
}
