package com.agentworkspace.approval;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="approval",schema="agent_workspace")
public class Approval {
 @Id private UUID id;
 @Column(name="workflow_id",nullable=false) private UUID workflowId;
 @Column(name="step_execution_id") private UUID stepExecutionId;
 @Column(name="artifact_id",nullable=false) private UUID artifactId;
 @Column(nullable=false) private String type;
 @Column(nullable=false) private String status;
 @Column(name="requested_at",nullable=false) private OffsetDateTime requestedAt;
 @Column(name="decided_at") private OffsetDateTime decidedAt;
 @Column(name="decided_by") private UUID decidedBy;
 private String comment;
 @Version private int version;
 protected Approval(){}
 public Approval(UUID workflowId,UUID stepExecutionId,UUID artifactId,String type){this.id=UUID.randomUUID();this.workflowId=workflowId;this.stepExecutionId=stepExecutionId;this.artifactId=artifactId;this.type=type;this.status="PENDING";this.requestedAt=OffsetDateTime.now();}
 public void decide(String decision,UUID actor,String comment){if(!"PENDING".equals(status))throw new IllegalStateException("Approval already decided");if(!"APPROVED".equals(decision)&&!"REJECTED".equals(decision))throw new IllegalArgumentException("Unsupported decision");if("REJECTED".equals(decision)&&(comment==null||comment.isBlank()))throw new IllegalArgumentException("Rejection comment required");status=decision;decidedBy=actor;decidedAt=OffsetDateTime.now();this.comment=comment;}
 public void supersede(){if("PENDING".equals(status))status="SUPERSEDED";}
 public UUID getId(){return id;}public UUID getWorkflowId(){return workflowId;}public UUID getStepExecutionId(){return stepExecutionId;}public UUID getArtifactId(){return artifactId;}public String getType(){return type;}public String getStatus(){return status;}public int getVersion(){return version;}public UUID getDecidedBy(){return decidedBy;}public String getComment(){return comment;}
}
