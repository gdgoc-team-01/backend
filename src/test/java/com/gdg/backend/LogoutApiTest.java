package com.gdg.backend;

import com.gdg.backend.domain.User;
import com.gdg.backend.domain.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class LogoutApiTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository users;

    @Test
    void loginPagesOfferGoogleSignIn() throws Exception {
        mvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("href=\"/oauth2/authorization/google\"")));
        mvc.perform(get("/login/failed"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(containsString("href=\"/oauth2/authorization/google\"")));
    }

    @Test
    void logoutRequiresCsrfAndEndsTheSession() throws Exception {
        String subject = "logout-test-subject";
        User member = users.save(User.fromGoogle("logout@knu.ac.kr", "로그아웃테스트", subject));
        var principal = new DefaultOidcUser(List.of(), OidcIdToken.withTokenValue("test").subject(subject).build());
        var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of());
        var session = new MockHttpSession();
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, new SecurityContextImpl(authentication));

        mvc.perform(get("/login/success").session(session))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/html"))
                .andExpect(content().string(containsString("로그아웃</button>")));
        mvc.perform(post("/logout").session(session))
                .andExpect(status().isForbidden());
        mvc.perform(get("/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(member.getId()));

        String csrf = mvc.perform(get("/csrf").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String header = JsonPath.read(csrf, "$.headerName");
        String token = JsonPath.read(csrf, "$.token");

        mvc.perform(post("/logout").session(session).header(header, token))
                .andExpect(status().isNoContent())
                .andExpect(cookie().maxAge("JSESSIONID", 0));
        assertTrue(session.isInvalid());
        mvc.perform(get("/me")).andExpect(status().isUnauthorized());
    }
}
