package com.agentworkspace.workflowdefinition;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WorkflowDefinitionRepository extends JpaRepository<WorkflowDefinition,UUID> {
    Optional<WorkflowDefinition> findByDefinitionKeyAndDefinitionVersion(String definitionKey,String definitionVersion);
}
