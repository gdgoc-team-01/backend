package com.gdg.backend.application;

import com.gdg.backend.domain.User;
import com.gdg.backend.domain.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class GoogleAccountService {

    private static final String SCHOOL_DOMAIN = "knu.ac.kr";

    private final UserRepository userRepository;
    private final UserService userService;

    public void registerIfNeeded(OidcIdToken token) {
        String email = token.getEmail();
        if (email == null || !Boolean.TRUE.equals(token.getEmailVerified())
                || !email.toLowerCase(Locale.ROOT).endsWith("@" + SCHOOL_DOMAIN)
                || !SCHOOL_DOMAIN.equalsIgnoreCase(token.getClaimAsString("hd"))) {
            throw new OAuth2AuthenticationException(new OAuth2Error("school_account_required"));
        }

        String subject = token.getSubject();
        if (userRepository.findByGoogleSubject(subject).isPresent()) {
            return;
        }

        if (userRepository.existsByEmailIgnoreCase(email)) {
            if (userRepository.findByGoogleSubject(subject).isPresent()) {
                return;
            }
            throw new OAuth2AuthenticationException(new OAuth2Error("account_link_required"));
        }

        try {
            userRepository.save(User.fromGoogle(email, userService.recommendNickname(), subject));
        } catch (DataIntegrityViolationException exception) {
            if (userRepository.findByGoogleSubject(subject).isEmpty()) {
                throw new OAuth2AuthenticationException(new OAuth2Error("account_creation_failed"), exception);
            }
        }
    }

    public User getUser(String googleSubject) {
        return userRepository.findByGoogleSubject(googleSubject)
                .orElseThrow(() -> new NotFoundException("연결된 사용자를 찾을 수 없습니다."));
    }
}
