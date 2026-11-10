package com.agentworkspace.workflow;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface WorkflowEventRepository extends JpaRepository<WorkflowEvent,UUID>{List<WorkflowEvent> findByWorkflowIdOrderByCreatedAtAsc(UUID id);}
