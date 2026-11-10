package com.agentworkspace.repo;

import com.agentworkspace.domain.Workflow;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface WorkflowRepository extends JpaRepository<Workflow,UUID> {
 @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
 @org.springframework.data.jpa.repository.Query("select w from Workflow w where w.id = :id")
 java.util.Optional<Workflow> lockById(@org.springframework.data.repository.query.Param("id") UUID id);}
