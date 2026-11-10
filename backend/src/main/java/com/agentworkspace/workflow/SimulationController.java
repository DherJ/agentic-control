package com.agentworkspace.workflow;
import com.agentworkspace.approval.*;
import com.agentworkspace.domain.*;
import java.security.Principal;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.context.annotation.Profile;
import org.springframework.http.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
@RestController
@Profile({"dev","test"})
@RequestMapping("/api/v1/dev/workflows")
public class SimulationController {
 private final WorkflowEngine engine;private final StepExecutionRepository steps;private final ArtifactService artifacts;private final WorkflowEventRepository events;private final ObjectMapper mapper;
 public SimulationController(WorkflowEngine engine,StepExecutionRepository steps,ArtifactService artifacts,WorkflowEventRepository events,ObjectMapper mapper){this.engine=engine;this.steps=steps;this.artifacts=artifacts;this.events=events;this.mapper=mapper;}
 public record Completion(@NotNull Map<String,Object> content){}
 public record Result(UUID artifactId,String type,int artifactVersion,String workflowStatus){}
 @PostMapping("/{workflowId}/steps/{stepId}/complete")
 @Transactional
 public Result complete(@PathVariable UUID workflowId,@PathVariable UUID stepId,@Valid @RequestBody Completion body,Principal principal){
  Workflow w=engine.lockAuthorized(workflowId,principal.getName());
  if(w.getStatus()!=WorkflowStatus.RUNNING)throw new ResponseStatusException(HttpStatus.CONFLICT,"Workflow is not running");
  StepExecution step=steps.findById(stepId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(!step.getWorkflowId().equals(workflowId)||step.getStatus()!=StepStatus.READY||!"AGENT".equals(step.getDefinition().getKind()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Step is not a ready agent step");
  var config=mapper.readTree(step.getDefinition().getConfigJson());String type=config.path("produces").asText();
  if(body.content()==null||body.content().isEmpty())throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Artifact content required");
  if("UX_MOCKUP_SET".equals(type)){
   Object proposals=body.content().get("proposals");
   if(!(proposals instanceof List<?> list)||list.size()<2||list.size()>config.path("maxProposals").asInt(3))throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"UX requires 2 to 3 proposals");
   Set<String> ids=new HashSet<>();for(Object o:(List<?>)proposals){if(!(o instanceof Map<?,?> m)||!(m.get("id") instanceof String id)||id.isBlank()||!ids.add(id))throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Each UX proposal needs a unique id");}
  }
  Artifact a=artifacts.record(workflowId,stepId,type,mapper.writeValueAsString(body.content()),principal.getName());
  step.markCompleted();events.save(new WorkflowEvent(workflowId,"AGENT_SIMULATED",null,"{}"));engine.advance(w);
  return new Result(a.getId(),a.getType(),a.getVersion(),w.getStatus().name());
 }
}
