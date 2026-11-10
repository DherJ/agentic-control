package com.agentworkspace.workflow;
import com.agentworkspace.domain.Workflow;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.security.Principal;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1")
public class WorkflowController {
 private final WorkflowEngine engine;
 public WorkflowController(WorkflowEngine engine){this.engine=engine;}
 public record NewProject(@NotBlank String name,String description,String githubRepository){}
 public record ProjectView(UUID id,String name){}
 public record NewWorkflow(@NotNull UUID projectId,@NotBlank String workflowDefinitionKey,@NotBlank String workflowDefinitionVersion,@NotBlank String title,@NotBlank String rawRequest){}
 public record WorkflowView(UUID id,UUID projectId,UUID workflowDefinitionId,String title,String status,int version){}
 public record StepView(UUID id,String key,String phase,String kind,String status,int iteration,int version){}
 public record EventView(UUID id,String type,String payload){}
 private WorkflowView view(Workflow w){return new WorkflowView(w.getId(),w.getProjectId(),w.getWorkflowDefinitionId(),w.getTitle(),w.getStatus().name(),w.getVersion());}
 @PostMapping("/projects") @ResponseStatus(HttpStatus.CREATED) public ProjectView createProject(@Valid @RequestBody NewProject req,Principal principal){Project p=engine.createProject(principal.getName(),req.name(),req.description(),req.githubRepository());return new ProjectView(p.getId(),p.getName());}
 @GetMapping("/projects") public List<ProjectView> projects(Principal principal){return engine.projects(principal.getName()).stream().map(p->new ProjectView(p.getId(),p.getName())).toList();}
 @PostMapping("/workflows") @ResponseStatus(HttpStatus.CREATED) public WorkflowView create(@Valid @RequestBody NewWorkflow req,Principal principal){return view(engine.create(principal.getName(),req.projectId(),req.workflowDefinitionKey(),req.workflowDefinitionVersion(),req.title(),req.rawRequest()));}
 @GetMapping("/workflows") public List<WorkflowView> list(Principal principal){return engine.list(principal.getName()).stream().map(this::view).toList();}
 @GetMapping("/workflows/{id}") public WorkflowView get(@PathVariable UUID id,Principal principal){return view(engine.authorizedWorkflow(id,principal.getName()));}
 @PostMapping("/workflows/{id}/start") public WorkflowView start(@PathVariable UUID id,Principal principal){return view(engine.start(id,principal.getName()));}
 @PostMapping("/workflows/{id}/cancel") public WorkflowView cancel(@PathVariable UUID id,Principal principal){return view(engine.cancel(id,principal.getName()));}
 @GetMapping("/workflows/{id}/steps") public List<StepView> steps(@PathVariable UUID id,Principal principal){return engine.steps(id,principal.getName()).stream().map(s->new StepView(s.getId(),s.getDefinition().getStepKey(),s.getDefinition().getPhaseKey(),s.getDefinition().getKind(),s.getStatus().name(),s.getIterationNumber(),s.getVersion())).toList();}
 @GetMapping("/workflows/{id}/events") public List<EventView> events(@PathVariable UUID id,Principal principal){return engine.events(id,principal.getName()).stream().map(e->new EventView(e.getId(),e.getEventType(),e.getPayload())).toList();}
}
