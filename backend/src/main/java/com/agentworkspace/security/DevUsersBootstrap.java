package com.agentworkspace.security;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Development-only demo credentials. Never enable dev profile on a public deployment. */
@Component
@Profile("dev")
public class DevUsersBootstrap implements ApplicationRunner {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;
    public DevUsersBootstrap(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }
    @Override @Transactional public void run(ApplicationArguments args) {
        createIfAbsent("jerome", "test", Role.ADMIN);
        createIfAbsent("invite", "invite", Role.INVITE);
    }
    private void createIfAbsent(String username, String password, Role role) {
        if (users.findByUsername(username).isEmpty()) {
            users.save(new AppUser(username, encoder.encode(password), role));
        }
    }
}
