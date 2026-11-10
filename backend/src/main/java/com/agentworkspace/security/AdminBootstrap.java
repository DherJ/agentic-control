package com.agentworkspace.security;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
@Component
public class AdminBootstrap implements ApplicationRunner {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    @Value("${app.bootstrap-admin.username:}") private String username;
    @Value("${app.bootstrap-admin.password:}") private String password;
    public AdminBootstrap(AppUserRepository users, PasswordEncoder encoder) { this.users=users; this.encoder=encoder; }
    @Override @Transactional public void run(ApplicationArguments args) {
        if (username.isBlank() && password.isBlank()) return;
        if (username.isBlank() || password.isBlank()) throw new IllegalStateException("Identifiants bootstrap incomplets");
        if (password.length()<16) throw new IllegalStateException("Mot de passe bootstrap trop court (16 caractères minimum)");
        if (users.findByUsername(username).isEmpty()) users.save(new AppUser(username, encoder.encode(password), Role.ADMIN));
    }
}
