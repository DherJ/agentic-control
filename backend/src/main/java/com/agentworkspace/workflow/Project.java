package com.agentworkspace.workflow;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity @Table(name="project",schema="agent_workspace")
public class Project {
 @Id private UUID id;
 @Column(name="owner_id",nullable=false) private UUID ownerId;
 @Column(nullable=false) private String name;
 @Column private String description;
 @Column(name="github_repository") private String githubRepository;
 @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
 @Column(name="updated_at",nullable=false) private OffsetDateTime updatedAt;
 protected Project(){}
 public Project(UUID ownerId,String name,String description,String githubRepository){id=UUID.randomUUID();this.ownerId=ownerId;this.name=name;this.description=description;this.githubRepository=githubRepository;createdAt=OffsetDateTime.now();updatedAt=createdAt;}
 public UUID getId(){return id;} public UUID getOwnerId(){return ownerId;} public String getName(){return name;}
}
