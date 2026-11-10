package com.agentworkspace.workflow;
import com.agentworkspace.workflowdefinition.StepDefinition;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="step_execution",schema="agent_workspace")
public class StepExecution {
 @Id private UUID id;
 @Column(name="workflow_id",nullable=false) private UUID workflowId;
 @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="step_definition_id") private StepDefinition definition;
 @Column(name="iteration_number",nullable=false) private int iterationNumber;
 @Enumerated(EnumType.STRING) @Column(nullable=false) private StepStatus status;
 @Column(name="started_at") private OffsetDateTime startedAt;
 @Column(name="finished_at") private OffsetDateTime finishedAt;
 @Version private int version;
 protected StepExecution() {}
 public StepExecution(UUID workflowId,StepDefinition definition) {this.id=UUID.randomUUID();this.workflowId=workflowId;this.definition=definition;this.iterationNumber=1;this.status=StepStatus.PENDING;}
 public UUID getId(){return id;} public UUID getWorkflowId(){return workflowId;} public StepDefinition getDefinition(){return definition;}
 public StepStatus getStatus(){return status;} public int getIterationNumber(){return iterationNumber;} public int getVersion(){return version;}
 public OffsetDateTime getStartedAt(){return startedAt;} public OffsetDateTime getFinishedAt(){return finishedAt;}
 public void markReady(){if(status!=StepStatus.PENDING)throw new IllegalStateException("Step not pending");status=StepStatus.READY;}
 public void awaitApproval(){if(status!=StepStatus.PENDING)throw new IllegalStateException("Step not pending");status=StepStatus.WAITING_APPROVAL;}
 public void markCompleted(){if(status!=StepStatus.PENDING && status!=StepStatus.READY)throw new IllegalStateException("Step not ready");status=StepStatus.COMPLETED;finishedAt=OffsetDateTime.now();}
 public void completeApproval(){if(status!=StepStatus.WAITING_APPROVAL)throw new IllegalStateException("Approval step not waiting");status=StepStatus.COMPLETED;finishedAt=OffsetDateTime.now();}
 public void rejectApproval(){if(status!=StepStatus.WAITING_APPROVAL)throw new IllegalStateException("Approval step not waiting");status=StepStatus.REJECTED;finishedAt=OffsetDateTime.now();}
 public void invalidate(){if(status!=StepStatus.CANCELLED){status=StepStatus.PENDING;startedAt=null;finishedAt=null;iterationNumber++;}}
 public void cancel(){if(status!=StepStatus.COMPLETED)status=StepStatus.CANCELLED;finishedAt=OffsetDateTime.now();}
}
