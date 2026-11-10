package com.agentworkspace.approval;
import com.agentworkspace.workflow.*;
import com.agentworkspace.domain.*;
import com.agentworkspace.security.*;
import java.util.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import tools.jackson.databind.ObjectMapper;
@Service
public class ApprovalService {
 private final ApprovalRepository approvals;private final ArtifactRepository artifacts;private final WorkflowEngine engine;private final AppUserRepository users;private final WorkflowEventRepository events;private final StepExecutionRepository steps;private final ObjectMapper mapper;private final ArtifactService artifactService;
 public ApprovalService(ApprovalRepository approvals,ArtifactRepository artifacts,WorkflowEngine engine,AppUserRepository users,WorkflowEventRepository events,StepExecutionRepository steps,ObjectMapper mapper,ArtifactService artifactService){this.approvals=approvals;this.artifacts=artifacts;this.engine=engine;this.users=users;this.events=events;this.steps=steps;this.mapper=mapper;this.artifactService=artifactService;}
 @Transactional(readOnly=true) public List<Approval> list(UUID workflowId,String username){engine.authorizedWorkflow(workflowId,username);return approvals.findByWorkflowIdOrderByRequestedAtAsc(workflowId);}
 @Transactional(readOnly=true) public Approval get(UUID id,String username){Approval a=approvals.findById(id).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));engine.authorizedWorkflow(a.getWorkflowId(),username);return a;}
 @Transactional public Approval request(UUID workflowId,UUID stepExecutionId,UUID artifactId,String type,String username){
  engine.lockAuthorized(workflowId,username);StepExecution step=steps.findById(stepExecutionId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  Artifact artifact=artifacts.findById(artifactId).orElseThrow(()->new ResponseStatusException(HttpStatus.NOT_FOUND));
  if(!step.getWorkflowId().equals(workflowId)||!artifact.getWorkflowId().equals(workflowId)||!"APPROVAL".equals(step.getDefinition().getKind())||step.getStatus()!=StepStatus.WAITING_APPROVAL)throw new ResponseStatusException(HttpStatus.CONFLICT,"Invalid approval step");
  var config=mapper.readTree(step.getDefinition().getConfigJson());
  if(!type.equals(config.path("approvalType").asText())||!artifact.getType().equals(config.path("artifact").asText()))throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Approval type mismatch");
  if(approvals.findByWorkflowIdAndStepExecutionIdAndStatus(workflowId,stepExecutionId,"PENDING").isPresent())throw new ResponseStatusException(HttpStatus.CONFLICT,"Approval already pending");
  Approval a=approvals.save(new Approval(workflowId,stepExecutionId,artifactId,type));events.save(new WorkflowEvent(workflowId,"APPROVAL_REQUESTED",null,"{}"));return a;
 }
 @Transactional public Approval decide(UUID id,String username,String decision,int expectedVersion,UUID artifactId,String comment,String selectedProposalId){
  Approval existing=get(id,username);Workflow w=engine.lockAuthorized(existing.getWorkflowId(),username);
  Approval a=approvals.lockById(id).orElseThrow();
  if(a.getVersion()!=expectedVersion||!a.getArtifactId().equals(artifactId)||!"PENDING".equals(a.getStatus()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Stale approval");
  Artifact reviewed=artifacts.findById(a.getArtifactId()).orElseThrow();
  var latest=artifacts.findByWorkflowIdAndTypeOrderByVersionDesc(w.getId(),reviewed.getType());
  if(latest.isEmpty()||!latest.getFirst().getId().equals(reviewed.getId()))throw new ResponseStatusException(HttpStatus.CONFLICT,"Superseded artifact");
  StepExecution step=steps.findById(a.getStepExecutionId()).orElseThrow();
  if(step.getStatus()!=StepStatus.WAITING_APPROVAL||w.getStatus()!=WorkflowStatus.WAITING_APPROVAL)throw new ResponseStatusException(HttpStatus.CONFLICT,"Approval step no longer active");
  AppUser actor=users.findByUsername(username).orElseThrow(()->new ResponseStatusException(HttpStatus.UNAUTHORIZED));
  if("APPROVED".equals(decision)&&"UX_DESIGN".equals(a.getType())){
   Artifact mockups=artifacts.findById(a.getArtifactId()).orElseThrow();
   if(selectedProposalId==null||selectedProposalId.isBlank())throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"UX selection required");
   var proposals=mapper.readTree(mockups.getContent()).path("proposals");boolean found=false;
   for(var proposal:proposals)if(selectedProposalId.equals(proposal.path("id").asText()))found=true;
   if(!found)throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,"Unknown UX proposal");
   artifactService.record(w.getId(),step.getId(),"UX_MOCKUP_SELECTION",mapper.writeValueAsString(Map.of("proposalId",selectedProposalId,"mockupSetArtifactId",mockups.getId().toString())),username);
  }
  try{a.decide(decision,actor.getId(),comment);}catch(IllegalArgumentException ex){throw new ResponseStatusException(HttpStatus.UNPROCESSABLE_ENTITY,ex.getMessage());}
  if("APPROVED".equals(decision)){step.completeApproval();engine.advance(w);}
  else {
   step.rejectApproval();var config=mapper.readTree(step.getDefinition().getConfigJson());String target=config.path("onReject").asText();
   List<StepExecution> all=steps.lockByWorkflowId(w.getId());
   // Invalidate the correction target and every transitively dependent step.
   Set<String> invalidated=new HashSet<>();invalidated.add(target);
   boolean progress;
   do {progress=false;for(StepExecution x:all){String dependency=mapper.readTree(x.getDefinition().getConfigJson()).path("requires").asText("");if(invalidated.contains(dependency)&&invalidated.add(x.getDefinition().getStepKey()))progress=true;}}while(progress);
   for(StepExecution x:all)if(invalidated.contains(x.getDefinition().getStepKey()))x.invalidate();
   for(Approval pending:approvals.findByWorkflowIdOrderByRequestedAtAsc(w.getId()))if("PENDING".equals(pending.getStatus()))pending.supersede();
   w.transitionTo(WorkflowStatus.RUNNING);engine.advance(w);
  }
  events.save(new WorkflowEvent(w.getId(),"APPROVAL_"+decision,actor.getId(),"{}"));return a;
 }
}
