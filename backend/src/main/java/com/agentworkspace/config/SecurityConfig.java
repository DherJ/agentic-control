package com.agentworkspace.config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.http.HttpStatus;
@Configuration
public class SecurityConfig {
    @Bean PasswordEncoder passwordEncoder() { return PasswordEncoderFactories.createDelegatingPasswordEncoder(); }
    @Bean SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/api/v1/auth/csrf", "/api/v1/auth/login").permitAll()
                .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/**").authenticated()
                .requestMatchers(org.springframework.http.HttpMethod.HEAD, "/api/v1/**").authenticated()
                .requestMatchers("/api/v1/**").hasRole("ADMIN")
                .anyRequest().authenticated())
            .exceptionHandling(ex -> ex.authenticationEntryPoint((request,response,exception) -> response.sendError(HttpStatus.UNAUTHORIZED.value())))
            .formLogin(form -> form.loginProcessingUrl("/api/v1/auth/login")
                .successHandler((request, response, authentication) -> response.setStatus(HttpStatus.NO_CONTENT.value()))
                .failureHandler((request, response, exception) -> response.sendError(HttpStatus.UNAUTHORIZED.value())))
            .logout(logout -> logout.logoutUrl("/api/v1/auth/logout").logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.NO_CONTENT.value())))
            .build(); // CSRF remains enabled
    }
}
