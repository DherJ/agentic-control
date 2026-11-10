package com.agentworkspace.workflow;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface StepExecutionRepository extends JpaRepository<StepExecution,UUID> {
 List<StepExecution> findByWorkflowId(UUID workflowId);
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select s from StepExecution s where s.workflowId = :workflowId")
 List<StepExecution> lockByWorkflowId(@org.springframework.data.repository.query.Param("workflowId") UUID workflowId);
}
