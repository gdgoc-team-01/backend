package com.gdg.backend;

import com.gdg.backend.application.GoogleAccountService;
import com.gdg.backend.domain.User;
import com.gdg.backend.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("local")
@Transactional
class GoogleAccountServiceTest {

    @Autowired
    private GoogleAccountService accounts;

    @Autowired
    private UserRepository users;

    @Test
    void repeatLoginUsesTheSameMember() {
        accounts.registerIfNeeded(token("student@knu.ac.kr", true, "knu.ac.kr"));
        User member = accounts.getUser("google-student-id");

        accounts.registerIfNeeded(token("renamed@knu.ac.kr", true, "knu.ac.kr"));

        assertEquals(member.getId(), accounts.getUser("google-student-id").getId());
        assertEquals("student@knu.ac.kr", member.getEmail());
    }

    @ParameterizedTest
    @CsvSource({
            "student@gmail.com, true,",
            "student@knu.ac.kr, true,",
            "student@knu.ac.kr, false, knu.ac.kr",
            "student@knu.ac.kr, true, other.edu",
            "student@fakeknu.ac.kr, true, knu.ac.kr"
    })
    void deniesAccountsOutsideTheSchoolPolicy(String email, boolean verified, String hostedDomain) {
        var exception = assertThrows(OAuth2AuthenticationException.class,
                () -> accounts.registerIfNeeded(token(email, verified, hostedDomain)));

        assertEquals("school_account_required", exception.getError().getErrorCode());
        assertTrue(users.findByGoogleSubject("google-student-id").isEmpty());
    }

    @Test
    void existingEmailDoesNotGrantAccessToTheMember() {
        users.save(new User("Student@knu.ac.kr", "기존회원"));

        var exception = assertThrows(OAuth2AuthenticationException.class,
                () -> accounts.registerIfNeeded(token("student@knu.ac.kr", true, "knu.ac.kr")));

        assertEquals("account_link_required", exception.getError().getErrorCode());
        assertTrue(users.findByGoogleSubject("google-student-id").isEmpty());
    }

    private OidcIdToken token(String email, boolean verified, String hostedDomain) {
        var token = OidcIdToken.withTokenValue("test-token")
                .subject("google-student-id")
                .claim("email", email)
                .claim("email_verified", verified);
        if (hostedDomain != null) {
            token.claim("hd", hostedDomain);
        }
        return token.build();
    }
}
