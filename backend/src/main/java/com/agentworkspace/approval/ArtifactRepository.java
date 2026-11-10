package com.agentworkspace.approval;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.*;
public interface ArtifactRepository extends JpaRepository<Artifact,UUID>{
 List<Artifact> findByWorkflowIdOrderByCreatedAtAsc(UUID workflowId);
 List<Artifact> findByWorkflowIdAndTypeOrderByVersionDesc(UUID workflowId,String type);
}
