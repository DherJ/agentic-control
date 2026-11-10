package com.agentworkspace.workflowdefinition;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StepDefinitionRepository extends JpaRepository<StepDefinition,UUID> {
    List<StepDefinition> findByWorkflowDefinitionIdOrderByStepKey(UUID workflowDefinitionId);
}
