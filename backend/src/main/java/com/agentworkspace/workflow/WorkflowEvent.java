package com.agentworkspace.workflow;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="workflow_event",schema="agent_workspace")
public class WorkflowEvent {
 @Id private UUID id;
 @Column(name="workflow_id",nullable=false) private UUID workflowId;
 @Column(name="event_type",nullable=false) private String eventType;
 @Column(name="actor_id") private UUID actorId;
 @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON) @Column(columnDefinition="jsonb",nullable=false) private String payload;
 @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
 protected WorkflowEvent() {}
 public WorkflowEvent(UUID workflowId,String eventType,UUID actorId,String payload){id=UUID.randomUUID();this.workflowId=workflowId;this.eventType=eventType;this.actorId=actorId;this.payload=payload;createdAt=OffsetDateTime.now();}
 public UUID getId(){return id;} public String getEventType(){return eventType;} public OffsetDateTime getCreatedAt(){return createdAt;} public String getPayload(){return payload;}
}
