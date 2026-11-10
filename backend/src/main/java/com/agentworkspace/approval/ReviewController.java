package com.agentworkspace.approval;
import java.security.Principal;
import java.util.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1")
public class ReviewController {
 private final ArtifactService artifacts;private final ApprovalService approvals;
 public ReviewController(ArtifactService artifacts,ApprovalService approvals){this.artifacts=artifacts;this.approvals=approvals;}
 public record ArtifactView(UUID id,UUID workflowId,String type,int version,String content,String contentHash){}
 public record ApprovalView(UUID id,UUID workflowId,UUID artifactId,String type,String status,int version,String comment){}
 public record Decision(@NotNull String decision,@NotNull Integer approvalVersion,@NotNull UUID artifactId,String comment,String selectedProposalId){}
 private ArtifactView view(Artifact a){return new ArtifactView(a.getId(),a.getWorkflowId(),a.getType(),a.getVersion(),a.getContent(),a.getContentHash());}
 private ApprovalView view(Approval a){return new ApprovalView(a.getId(),a.getWorkflowId(),a.getArtifactId(),a.getType(),a.getStatus(),a.getVersion(),a.getComment());}
 @GetMapping("/workflows/{id}/artifacts") public List<ArtifactView> artifacts(@PathVariable UUID id,Principal p){return artifacts.list(id,p.getName()).stream().map(this::view).toList();}
 @GetMapping("/workflows/{id}/artifacts/{type}/versions") public List<ArtifactView> versions(@PathVariable UUID id,@PathVariable String type,Principal p){return artifacts.versions(id,type,p.getName()).stream().map(this::view).toList();}
 @GetMapping("/artifacts/{id}") public ArtifactView artifact(@PathVariable UUID id,Principal p){return view(artifacts.get(id,p.getName()));}
 @GetMapping("/workflows/{id}/approvals") public List<ApprovalView> approvals(@PathVariable UUID id,Principal p){return approvals.list(id,p.getName()).stream().map(this::view).toList();}
 @GetMapping("/approvals/{id}") public ApprovalView approval(@PathVariable UUID id,Principal p){return view(approvals.get(id,p.getName()));}
 @PostMapping("/approvals/{id}/decision") public ApprovalView decide(@PathVariable UUID id,@Valid @RequestBody Decision d,Principal p){return view(approvals.decide(id,p.getName(),d.decision(),d.approvalVersion(),d.artifactId(),d.comment(),d.selectedProposalId()));}
}
