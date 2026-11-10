package com.agentworkspace.agent;

import com.agentworkspace.approval.*;
import com.agentworkspace.domain.WorkflowStatus;
import com.agentworkspace.workflow.*;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;

@Service
public class AnalystPersistence {
 private final WorkflowEngine engine;private final StepExecutionRepository steps;private final ArtifactService artifacts;private final WorkflowEventRepository events;private final ObjectMapper mapper;
 public AnalystPersistence(WorkflowEngine engine,StepExecutionRepository steps,ArtifactService artifacts,WorkflowEventRepository events,ObjectMapper mapper){this.engine=engine;this.steps=steps;this.artifacts=artifacts;this.events=events;this.mapper=mapper;}
 @Transactional
 public AnalystController.RunResult complete(UUID workflowId,UUID stepId,String username,AgentExecutionResult output){
  var workflow=engine.lockAuthorized(workflowId,username);
  var step=steps.lockByWorkflowId(workflowId).stream().filter(s->s.getId().equals(stepId)).findFirst().orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(workflow.getStatus()!=WorkflowStatus.RUNNING||step.getStatus()!=StepStatus.READY||!"project-brief".equals(step.getDefinition().getStepKey()))
   throw new ResponseStatusException(HttpStatus.CONFLICT,"Étape modifiée pendant l'appel IA");
  Artifact artifact=artifacts.record(workflowId,stepId,"PROJECT_BRIEF",mapper.writeValueAsString(output.output()),username);
  step.markCompleted();events.save(new WorkflowEvent(workflowId,"AGENT_COMPLETED",null,"{}"));engine.advance(workflow);
  return new AnalystController.RunResult(artifact.getId(),artifact.getType(),artifact.getVersion(),workflow.getStatus().name());
 }
}
