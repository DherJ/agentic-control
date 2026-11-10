package com.agentworkspace.agent;

import com.agentworkspace.domain.WorkflowStatus;
import com.agentworkspace.finops.AiFinopsService;
import com.agentworkspace.workflow.*;
import java.security.Principal;
import java.util.Map;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/workflows")
public class AnalystController {
 private final WorkflowEngine engine;
 private final StepExecutionRepository steps;
 private final OpenAiAnalystExecutor analyst;
 private final AiFinopsService finops;
 public AnalystController(WorkflowEngine engine,StepExecutionRepository steps,OpenAiAnalystExecutor analyst,AnalystPersistence persistence,AiFinopsService finops){
  this.engine=engine;this.steps=steps;this.analyst=analyst;this.persistence=persistence;this.finops=finops;
 }
 public record RunResult(UUID artifactId,String artifactType,int version,String workflowStatus){}
 @PostMapping("/{workflowId}/steps/{stepId}/run-analyst")
 public RunResult run(@PathVariable UUID workflowId,@PathVariable UUID stepId,Principal principal){
  // Preflight: no long DB transaction held during external API call.
  var workflow=engine.authorizedWorkflow(workflowId,principal.getName());
  var step=steps.findById(stepId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  validate(workflowId,workflow.getStatus(),step);
  UUID usageId=finops.reserve(workflow.getProjectId(),workflowId,stepId,workflow.getRawRequest());
  AgentExecutionResult output;
  try {
   output=analyst.execute(new AgentExecutionContext(workflowId,stepId,AgentType.ANALYST,Map.of("request",workflow.getRawRequest())));
  }catch(Exception ex){finops.finish(usageId,new AgentExecutionResult(false,"PROJECT_BRIEF",Map.of(),"Erreur d'exécution"));throw ex;}
  finops.finish(usageId,output);
  if(!output.success())throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,output.errorMessage());
  return persist(workflowId,stepId,principal.getName(),output);
 }
 // The transactional boundary must be crossed through a Spring proxy, not self-invocation.
 private RunResult persist(UUID workflowId,UUID stepId,String username,AgentExecutionResult output){return persistence.complete(workflowId,stepId,username,output);}
 private final AnalystPersistence persistence;
 private void validate(UUID workflowId,WorkflowStatus status,StepExecution step){
  if(status!=WorkflowStatus.RUNNING||!step.getWorkflowId().equals(workflowId)||step.getStatus()!=StepStatus.READY||!"AGENT".equals(step.getDefinition().getKind())||!"project-brief".equals(step.getDefinition().getStepKey()))
   throw new ResponseStatusException(HttpStatus.CONFLICT,"L'étape Analyste n'est pas prête");
 }
}
