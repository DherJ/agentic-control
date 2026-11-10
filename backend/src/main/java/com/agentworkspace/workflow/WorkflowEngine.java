package com.agentworkspace.workflow;
import com.agentworkspace.domain.*;
import com.agentworkspace.repo.WorkflowRepository;
import com.agentworkspace.security.*;
import com.agentworkspace.workflowdefinition.*;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
@Service
public class WorkflowEngine {
 private final WorkflowRepository workflows; private final ProjectRepository projects; private final WorkflowDefinitionRepository definitions;
 private final StepDefinitionRepository stepDefinitions; private final StepExecutionRepository executions;
 private final WorkflowEventRepository events; private final AppUserRepository users; private final ObjectMapper mapper; private final com.agentworkspace.approval.ApprovalRepository approvals; private final com.agentworkspace.approval.ArtifactRepository artifacts;
 public WorkflowEngine(WorkflowRepository workflows,ProjectRepository projects,WorkflowDefinitionRepository definitions,StepDefinitionRepository stepDefinitions,StepExecutionRepository executions,WorkflowEventRepository events,AppUserRepository users,ObjectMapper mapper,com.agentworkspace.approval.ApprovalRepository approvals,com.agentworkspace.approval.ArtifactRepository artifacts){
 this.workflows=workflows;this.projects=projects;this.definitions=definitions;this.stepDefinitions=stepDefinitions;this.executions=executions;this.events=events;this.users=users;this.mapper=mapper;this.approvals=approvals;this.artifacts=artifacts;}
 public Workflow lockAuthorized(UUID id,String username){authorizedWorkflow(id,username);return workflows.lockById(id).orElseThrow();}
 private AppUser current(String username){return users.findByUsername(username).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));}
 private boolean admin(AppUser user){return user.getRole()==Role.ADMIN;}
 private Project authorizedProject(UUID id,AppUser user){Project p=projects.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));if(!admin(user)&&user.getRole()!=Role.INVITE&&!p.getOwnerId().equals(user.getId()))throw new ResponseStatusException(HttpStatus.FORBIDDEN);return p;}
 public Workflow authorizedWorkflow(UUID id,String username){AppUser user=current(username);Workflow w=workflows.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));authorizedProject(w.getProjectId(),user);return w;}
 @Transactional public Project createProject(String username,String name,String description,String repository){AppUser user=current(username);if(name==null||name.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Project name required");return projects.save(new Project(user.getId(),name,description,repository));}
 @Transactional(readOnly=true) public List<Project> projects(String username){AppUser user=current(username);return admin(user)||user.getRole()==Role.INVITE?projects.findAll():projects.findByOwnerId(user.getId());}
 @Transactional public Workflow create(String username,UUID projectId,String definitionKey,String definitionVersion,String title,String rawRequest){
 AppUser user=current(username);authorizedProject(projectId,user);
 if(title==null||title.isBlank()||rawRequest==null||rawRequest.isBlank())throw new ResponseStatusException(HttpStatus.BAD_REQUEST,"Title and request required");
 WorkflowDefinition definition=definitions.findByDefinitionKeyAndDefinitionVersion(definitionKey,definitionVersion).orElseThrow(()->new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Definition unavailable"));
 Workflow w=new Workflow(projectId,title,rawRequest,user.getId());w.assignDefinition(definition.getId());workflows.save(w);
 for(StepDefinition step:stepDefinitions.findByWorkflowDefinitionIdOrderByStepKey(definition.getId()))executions.save(new StepExecution(w.getId(),step));
 events.save(new WorkflowEvent(w.getId(),"WORKFLOW_CREATED",user.getId(),"{}"));return w;}
 @Transactional public Workflow start(UUID id,String username){Workflow w=authorizedWorkflow(id,username);if(w.getStatus()!=WorkflowStatus.DRAFT)throw new ResponseStatusException(HttpStatus.CONFLICT,"Workflow already started");
 w.transitionTo(WorkflowStatus.RUNNING);events.save(new WorkflowEvent(w.getId(),"WORKFLOW_STARTED",current(username).getId(),"{}"));advance(w);return w;}
 // 1C: only the dependency scheduler runs; AGENT steps remain READY, APPROVAL steps cannot be bypassed.
 public void advance(Workflow w){
 List<StepExecution> steps=executions.findByWorkflowId(w.getId());Map<String,StepExecution> byKey=steps.stream().collect(Collectors.toMap(s->s.getDefinition().getStepKey(),s->s,(a,b)->a.getIterationNumber()>=b.getIterationNumber()?a:b));
 boolean changed;
 do {changed=false;for(StepExecution step:steps){if(step.getStatus()!=StepStatus.PENDING)continue;
 String config=step.getDefinition().getConfigJson();String required=mapper.readTree(config).path("requires").asText(null);
 if(required!=null && (!byKey.containsKey(required)||byKey.get(required).getStatus()!=StepStatus.COMPLETED))continue;
 switch(step.getDefinition().getKind()){
 case "AGENT" -> {step.markReady();events.save(new WorkflowEvent(w.getId(),"STEP_READY",null,"{}"));}
 case "APPROVAL" -> {
  var cfg=mapper.readTree(config);String type=cfg.path("artifact").asText();String approvalType=cfg.path("approvalType").asText();
  var latest=artifacts.findByWorkflowIdAndTypeOrderByVersionDesc(w.getId(),type).stream().findFirst().orElseThrow(()->new IllegalStateException("Missing artifact: "+type));
  step.awaitApproval();
  if(approvals.findByWorkflowIdAndStepExecutionIdAndStatus(w.getId(),step.getId(),"PENDING").isPresent())throw new IllegalStateException("Duplicate pending approval");
  approvals.save(new com.agentworkspace.approval.Approval(w.getId(),step.getId(),latest.getId(),approvalType));
  w.transitionTo(WorkflowStatus.WAITING_APPROVAL);events.save(new WorkflowEvent(w.getId(),"APPROVAL_REQUESTED",null,"{}"));
 }
 case "HANDOFF" -> {step.markCompleted();w.transitionTo(WorkflowStatus.COMPLETED);events.save(new WorkflowEvent(w.getId(),"WORKFLOW_COMPLETED",null,"{}"));}
 default -> throw new IllegalStateException("Unknown step kind");
 }changed=true;
 }}while(changed);
 if(w.getStatus()!=WorkflowStatus.COMPLETED && w.getStatus()!=WorkflowStatus.CANCELLED){boolean waiting=steps.stream().anyMatch(x->x.getStatus()==StepStatus.WAITING_APPROVAL);w.transitionTo(waiting?WorkflowStatus.WAITING_APPROVAL:WorkflowStatus.RUNNING);}
 }
 @Transactional public Workflow cancel(UUID id,String username){Workflow w=authorizedWorkflow(id,username);if(w.getStatus()==WorkflowStatus.COMPLETED||w.getStatus()==WorkflowStatus.CANCELLED)throw new ResponseStatusException(HttpStatus.CONFLICT,"Workflow is terminal");w.transitionTo(WorkflowStatus.CANCELLED);for(StepExecution s:executions.findByWorkflowId(id))if(s.getStatus()!=StepStatus.COMPLETED)s.cancel();events.save(new WorkflowEvent(id,"WORKFLOW_CANCELLED",current(username).getId(),"{}"));return w;}
 @Transactional(readOnly=true) public List<StepExecution> steps(UUID id,String username){authorizedWorkflow(id,username);return executions.findByWorkflowId(id);}
 @Transactional(readOnly=true) public List<WorkflowEvent> events(UUID id,String username){authorizedWorkflow(id,username);return events.findByWorkflowIdOrderByCreatedAtAsc(id);}
 @Transactional(readOnly=true) public List<Workflow> list(String username){AppUser user=current(username);return workflows.findAll().stream().filter(w->admin(user)||user.getRole()==Role.INVITE||projects.findById(w.getProjectId()).map(p->p.getOwnerId().equals(user.getId())).orElse(false)).toList();}
}
