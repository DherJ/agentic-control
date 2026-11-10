package com.agentworkspace.approval;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="artifact",schema="agent_workspace")
public class Artifact {
 @Id private UUID id;
 @Column(name="workflow_id",nullable=false) private UUID workflowId;
 @Column(name="step_execution_id") private UUID stepExecutionId;
 @Column(nullable=false) private String type;
 @Column(nullable=false) private int version;
 @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON) @Column(nullable=false,columnDefinition="jsonb") private String content;
 @Column(name="content_hash",nullable=false) private String contentHash;
 @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
 protected Artifact(){}
 public Artifact(UUID workflowId,UUID stepExecutionId,String type,int version,String content,String hash){this.id=UUID.randomUUID();this.workflowId=workflowId;this.stepExecutionId=stepExecutionId;this.type=type;this.version=version;this.content=content;this.contentHash=hash;this.createdAt=OffsetDateTime.now();}
 public UUID getId(){return id;}public UUID getWorkflowId(){return workflowId;}public UUID getStepExecutionId(){return stepExecutionId;}public String getType(){return type;}public int getVersion(){return version;}public String getContent(){return content;}public String getContentHash(){return contentHash;}public OffsetDateTime getCreatedAt(){return createdAt;}
}
