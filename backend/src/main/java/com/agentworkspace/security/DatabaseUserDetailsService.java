package com.agentworkspace.security;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
@Service
public class DatabaseUserDetailsService implements UserDetailsService {
    private final AppUserRepository users;
    public DatabaseUserDetailsService(AppUserRepository users) { this.users=users; }
    @Override public UserDetails loadUserByUsername(String username) {
        AppUser user=users.findByUsername(username).orElseThrow(() -> new UsernameNotFoundException("Utilisateur introuvable"));
        return User.withUsername(user.getUsername()).password(user.getPasswordHash())
            .roles(user.getRole().name()).disabled(!user.isEnabled()).build();
    }
}
