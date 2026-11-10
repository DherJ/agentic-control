package com.agentworkspace.security;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
@Entity
@Table(name = "app_user", schema = "agent_workspace")
public class AppUser {
    @Id private UUID id;
    @Column(nullable=false,unique=true,length=100) private String username;
    @Column(name="password_hash",nullable=false,length=255) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private Role role;
    @Column(nullable=false) private boolean enabled;
    @Column(name="created_at",nullable=false) private OffsetDateTime createdAt;
    protected AppUser() {}
    public AppUser(String username, String passwordHash, Role role) {
        this.id=UUID.randomUUID(); this.username=username; this.passwordHash=passwordHash;
        this.role=role; this.enabled=true; this.createdAt=OffsetDateTime.now();
    }
    public UUID getId(){return id;}
    public String getUsername(){return username;}
    public String getPasswordHash(){return passwordHash;}
    public Role getRole(){return role;}
    public boolean isEnabled(){return enabled;}
}
