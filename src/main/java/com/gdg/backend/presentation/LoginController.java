package com.gdg.backend.presentation;

import com.gdg.backend.application.GoogleAccountService;
import com.gdg.backend.presentation.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
class LoginController {

    private final GoogleAccountService accounts;

    @GetMapping(value = "/", produces = "text/html;charset=UTF-8")
    Resource loginPage() {
        return new ClassPathResource("templates/login.html");
    }

    @GetMapping(value = "/login/success", produces = "text/html;charset=UTF-8")
    Resource success() {
        return new ClassPathResource("templates/login-success.html");
    }

    @GetMapping(value = "/login/failed", produces = "text/html;charset=UTF-8")
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    Resource failure() {
        return loginPage();
    }

    @GetMapping("/me")
    UserResponse me(@AuthenticationPrincipal OidcUser principal) {
        var user = accounts.getUser(principal.getSubject());
        return new UserResponse(user.getId(), user.getNickname());
    }

    @GetMapping("/csrf")
    CsrfResponse csrf(CsrfToken csrfToken) {
        return new CsrfResponse(csrfToken.getHeaderName(), csrfToken.getToken());
    }

    record CsrfResponse(String headerName, String token) {
    }
}
