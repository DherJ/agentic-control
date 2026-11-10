package com.agentworkspace.finops;
import com.agentworkspace.workflow.WorkflowEngine;
import java.math.BigDecimal;
import java.security.Principal;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/projects/{projectId}/ai-costs")
public class AiFinopsController {
 private final AiFinopsService service;private final WorkflowEngine engine;
 public AiFinopsController(AiFinopsService service,WorkflowEngine engine){this.service=service;this.engine=engine;}
 private void authorize(UUID projectId,Principal principal){
  // Use the same project membership/ownership visibility as existing project listing.
  if(engine.projects(principal.getName()).stream().noneMatch(p->p.getId().equals(projectId)))
   throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND);
 }
 @GetMapping public AiFinopsService.Summary summary(@PathVariable UUID projectId,Principal principal,@RequestParam(required=false) String model,@RequestParam(required=false) String status,@RequestParam(required=false) String search){authorize(projectId,principal);return service.summary(projectId,model,status,search);}
 public record BudgetUpdate(BigDecimal budgetUsd,BigDecimal requestLimitUsd,int alertPercent){}
 @PutMapping("/budget") public AiFinopsService.Budget update(@PathVariable UUID projectId,Principal principal,@RequestBody BudgetUpdate body){authorize(projectId,principal);return service.update(projectId,body.budgetUsd(),body.requestLimitUsd(),body.alertPercent());}
}
