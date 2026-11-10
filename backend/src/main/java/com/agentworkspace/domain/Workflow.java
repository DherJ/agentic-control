package com.agentworkspace.domain;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="workflow",schema="agent_workspace")
public class Workflow {
    @Id private UUID id;
    @Column(name="project_id",nullable=false) private UUID projectId;
    @Column(name="workflow_definition_id") private UUID workflowDefinitionId;
    @Column(nullable=false,length=200) private String title;
    @Column(name="raw_request",nullable=false,columnDefinition="text") private String rawRequest;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=50) private WorkflowStatus status;
    @Enumerated(EnumType.STRING) @Column(name="resume_status",length=50) private WorkflowStatus resumeStatus;
    @Version private int version;
    @Column(name="created_by",nullable=false) private UUID createdBy;
    @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
    @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
    @Column(name="completed_at") private OffsetDateTime completedAt;
    protected Workflow() {}
    public Workflow(UUID projectId, String title, String rawRequest, UUID createdBy) {
        this.id=UUID.randomUUID(); this.projectId=projectId; this.title=title;
        this.rawRequest=rawRequest; this.createdBy=createdBy; this.status=WorkflowStatus.DRAFT;
        this.createdAt=OffsetDateTime.now(); this.updatedAt=this.createdAt;
    }
    public UUID getId(){return id;}
    public UUID getProjectId(){return projectId;}
    public UUID getWorkflowDefinitionId(){return workflowDefinitionId;}
    public void assignDefinition(UUID id){if(workflowDefinitionId!=null)throw new IllegalStateException("Definition already set");workflowDefinitionId=id;}
    public String getTitle(){return title;}
    public String getRawRequest(){return rawRequest;}
    public WorkflowStatus getStatus(){return status;}
    public int getVersion(){return version;}
    public void transitionTo(WorkflowStatus next){this.status=next;this.updatedAt=OffsetDateTime.now();}
}
