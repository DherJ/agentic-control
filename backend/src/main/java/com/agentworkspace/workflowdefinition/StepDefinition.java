package com.agentworkspace.workflowdefinition;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name="step_definition",schema="agent_workspace",uniqueConstraints=@UniqueConstraint(columnNames={"workflow_definition_id","step_key"}))
public class StepDefinition {
    @Id private UUID id;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="workflow_definition_id",nullable=false)
    private WorkflowDefinition workflowDefinition;
    @Column(name="step_key",nullable=false,length=100) private String stepKey;
    @Column(name="phase_key",nullable=false,length=100) private String phaseKey;
    @Column(name="kind",nullable=false,length=30) private String kind;
    @Column(name="config_json",nullable=false,columnDefinition="jsonb") @org.hibernate.annotations.JdbcTypeCode(org.hibernate.type.SqlTypes.JSON)
    private String configJson;
    protected StepDefinition() {}
    public StepDefinition(WorkflowDefinition definition,String key,String phase,String kind,String configJson) {
        this.id=UUID.randomUUID(); this.workflowDefinition=definition; this.stepKey=key;
        this.phaseKey=phase; this.kind=kind; this.configJson=configJson;
    }
    public UUID getId(){return id;}
    public String getStepKey(){return stepKey;}
    public String getPhaseKey(){return phaseKey;}
    public String getKind(){return kind;}
    public String getConfigJson(){return configJson;}
}
