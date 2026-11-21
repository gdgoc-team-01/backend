package com.gdg.backend;

import com.gdg.backend.domain.User;
import com.gdg.backend.domain.UserRepository;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
@Transactional
class UserApiTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MockMvc mvc;

    static Stream<Arguments> invalidNicknames() {
        return Stream.of(
                Arguments.of("빈 문자열", "{\"nickname\":\"\"}", "닉네임을 입력해주세요."),
                Arguments.of("공백", "{\"nickname\":\"   \"}", "닉네임을 입력해주세요."),
                Arguments.of("null", "{\"nickname\":null}", "닉네임을 입력해주세요."),
                Arguments.of("필드 누락", "{}", "닉네임을 입력해주세요."),
                Arguments.of("13자", "{\"nickname\":\"가나다라마바사아자차카타파\"}", "닉네임은 12자 이하여야 합니다.")
        );
    }

    @ParameterizedTest(name = "{0} 닉네임은 400으로 거절")
    @MethodSource("invalidNicknames")
    void invalidNicknameReturns400(String scenario, String body, String expectedMessage) throws Exception {
        User user = userRepository.save(User.fromGoogle("validation@example.com", "행복한 수달", "validation"));

        mutateAs("validation", patch("/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(expectedMessage));
        assertEquals("행복한 수달", user.getNickname());
    }

    @Test
    void twelveCharacterNicknameIsAccepted() throws Exception {
        User user = userRepository.save(User.fromGoogle("boundary@example.com", "행복한 수달", "boundary"));
        String nickname = "가나다라마바사아자차카타";

        mutateAs("boundary", patch("/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"" + nickname + "\"}"))
                .andExpect(status().isOk());
        assertEquals(nickname, userRepository.findById(user.getId()).orElseThrow().getNickname());
    }

    @Test
    void duplicateNicknameReturns409() throws Exception {
        User user = userRepository.save(User.fromGoogle("first@example.com", "행복한 수달", "first"));
        User other = userRepository.save(new User("second@example.com", "졸린 고양이"));

        mutateAs("first", patch("/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"졸린 고양이\"}"))
                .andExpect(status().isConflict())
                .andExpect(content().string("이미 존재하는 닉네임입니다."));
        assertEquals("행복한 수달", user.getNickname());
        assertEquals("졸린 고양이", other.getNickname());
    }

    @Test
    void onlyOwnerCanModifyOrDeleteTheProfile() throws Exception {
        User owner = userRepository.save(User.fromGoogle("owner@example.com", "원래 닉네임", "owner"));
        userRepository.save(User.fromGoogle("other@example.com", "다른 닉네임", "other"));

        mvc.perform(delete("/users/{id}", owner.getId())).andExpect(status().isForbidden());
        mutateAs("other", patch("/users/{id}", owner.getId())
                .contentType(MediaType.APPLICATION_JSON).content("{\"nickname\":\"변경 시도\"}"))
                .andExpect(status().isForbidden());
        mutateAs("other", delete("/users/{id}", owner.getId()))
                .andExpect(status().isForbidden());
        assertEquals("원래 닉네임", userRepository.findById(owner.getId()).orElseThrow().getNickname());

        mutateAs("owner", delete("/users/{id}", owner.getId())).andExpect(status().isOk());
        assertFalse(userRepository.existsById(owner.getId()));
    }

    private ResultActions mutateAs(String subject, MockHttpServletRequestBuilder request) throws Exception {
        var token = OidcIdToken.withTokenValue("test").subject(subject).build();
        var principal = new DefaultOidcUser(List.of(), token);
        var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of());
        var session = new MockHttpSession();
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, new SecurityContextImpl(authentication));

        String csrf = mvc.perform(get("/csrf").session(session))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String header = JsonPath.read(csrf, "$.headerName");
        String csrfToken = JsonPath.read(csrf, "$.token");
        return mvc.perform(request.session(session).header(header, csrfToken));
    }
}
