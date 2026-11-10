package com.agentworkspace.workflow;
import java.util.*;
import org.springframework.data.jpa.repository.JpaRepository;
public interface ProjectRepository extends JpaRepository<Project,UUID>{List<Project> findByOwnerId(UUID ownerId);}
