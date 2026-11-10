package com.agentworkspace.approval;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ApprovalRepository extends JpaRepository<Approval,UUID>{
 List<Approval> findByWorkflowIdOrderByRequestedAtAsc(UUID workflowId);
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select a from Approval a where a.id = :id")
 Optional<Approval> lockById(@org.springframework.data.repository.query.Param("id") UUID id);
 List<Approval> findByStatusOrderByRequestedAtAsc(String status);
 Optional<Approval> findByWorkflowIdAndStepExecutionIdAndStatus(UUID workflowId,UUID stepExecutionId,String status);
}
