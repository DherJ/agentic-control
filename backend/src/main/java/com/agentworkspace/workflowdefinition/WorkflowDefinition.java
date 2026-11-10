package com.agentworkspace.workflowdefinition;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(name="workflow_definition",schema="agent_workspace",uniqueConstraints=@UniqueConstraint(columnNames={"definition_key","definition_version"}))
public class WorkflowDefinition {
    @Id private UUID id;
    @Column(name="definition_key",nullable=false,length=100) private String definitionKey;
    @Column(name="definition_version",nullable=false,length=50) private String definitionVersion;
    @Column(name="name",nullable=false,length=200) private String name;
    @Column(name="definition_json",nullable=false,columnDefinition="jsonb") @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON) private String definitionJson;
    @Column(name="checksum",nullable=false,length=64) private String checksum;
    @Column(name="published_at",nullable=false) private OffsetDateTime publishedAt;
    protected WorkflowDefinition() {}
    public WorkflowDefinition(String key,String version,String name,String json,String checksum) {
        this.id=UUID.randomUUID(); this.definitionKey=key; this.definitionVersion=version;
        this.name=name; this.definitionJson=json; this.checksum=checksum; this.publishedAt=OffsetDateTime.now();
    }
    public UUID getId(){return id;}
    public String getDefinitionKey(){return definitionKey;}
    public String getDefinitionVersion(){return definitionVersion;}
    public String getName(){return name;}
    public String getDefinitionJson(){return definitionJson;}
    public String getChecksum(){return checksum;}
    public OffsetDateTime getPublishedAt(){return publishedAt;}
}
