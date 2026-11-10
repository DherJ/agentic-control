package com.agentworkspace.security;

import java.security.Principal;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/auth")
public class SessionController {
    public record SessionView(String username, List<String> authorities) {}
    public record CsrfView(String token, String headerName) {}

    @GetMapping("/me")
    public SessionView me(Principal principal) {
        if (!(principal instanceof Authentication auth)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        return new SessionView(auth.getName(), auth.getAuthorities().stream().map(Object::toString).toList());
    }

    @GetMapping("/csrf")
    public CsrfView csrf(HttpServletRequest request) {
        CsrfToken csrfToken = (CsrfToken) request.getAttribute(CsrfToken.class.getName());
        if (csrfToken == null) throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "CSRF token unavailable");
        return new CsrfView(csrfToken.getToken(), csrfToken.getHeaderName());
    }
}
