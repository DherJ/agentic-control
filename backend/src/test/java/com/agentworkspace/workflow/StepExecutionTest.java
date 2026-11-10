package com.agentworkspace.workflow;
import com.agentworkspace.workflowdefinition.StepDefinition;
import com.agentworkspace.workflowdefinition.WorkflowDefinition;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class StepExecutionTest {
 private StepDefinition definition(String kind){
   WorkflowDefinition wf=new WorkflowDefinition("bmad-standard","1.0.0","BMAD","{}","a".repeat(64));
   return new StepDefinition(wf,"step","analysis",kind,"{}");
 }
 @Test void agentStepStartsPendingAndBecomesReady(){
   StepExecution step=new StepExecution(UUID.randomUUID(),definition("AGENT"));
   assertEquals(StepStatus.PENDING,step.getStatus());
   step.markReady();assertEquals(StepStatus.READY,step.getStatus());
   assertThrows(IllegalStateException.class,step::markReady);
 }
 @Test void approvalIsNeverAutomaticallyCompleted(){
   StepExecution step=new StepExecution(UUID.randomUUID(),definition("APPROVAL"));
   step.awaitApproval();assertEquals(StepStatus.WAITING_APPROVAL,step.getStatus());
   assertThrows(IllegalStateException.class,step::markCompleted);
 }
 @Test void cancelPreservesCompletedSteps(){
   StepExecution step=new StepExecution(UUID.randomUUID(),definition("HANDOFF"));
   step.markCompleted();step.cancel();assertEquals(StepStatus.COMPLETED,step.getStatus());
 }
}
