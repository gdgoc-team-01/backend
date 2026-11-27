package com.gdg.backend;

import com.gdg.backend.domain.User;
import com.gdg.backend.domain.UserRepository;
import com.jayway.jsonpath.JsonPath;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.oauth2.core.oidc.OidcIdToken;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.AbstractMockHttpServletRequestBuilder;

import static org.springframework.security.web.context.HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("local")
class PostApiTest {
    @Autowired MockMvc mvc;
    @Autowired UserRepository users;

    @Test
    void foundPostCanBeCreatedReadEditedAndDeletedByItsAuthor() throws Exception {
        MockHttpSession owner = session("post-owner");
        MockHttpSession other = session("post-reader");
        var buffer = new ByteArrayOutputStream();
        ImageIO.write(new BufferedImage(512, 512, BufferedImage.TYPE_INT_RGB), "png", buffer);
        byte[] image = buffer.toByteArray();
        var create = multipart("/api/posts").file("image", image);
        var created = mvc.perform(signed(owner, found(create, "지갑")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.zoneLabel").value("zone2"))
                .andExpect(jsonPath("$.time.foundAt").exists()).andReturn().getResponse();
        String id = JsonPath.read(created.getContentAsString(), "$.id");
        String url = "/api/posts/" + id;
        mvc.perform(get(url + "/image")).andExpect(status().isOk()).andExpect(content().bytes(image));
        mvc.perform(get("/api/posts").param("kind", "found").param("zoneLabel", "zone2"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].id").value(id));
        mvc.perform(get("/api/posts").param("kind", "lost"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[?(@.id == '" + id + "')]").isEmpty());
        mvc.perform(signed(other, delete(url))).andExpect(status().isForbidden());
        mvc.perform(signed(owner, found(multipart(HttpMethod.PUT, url), "검은 지갑")))
                .andExpect(status().isOk()).andExpect(jsonPath("$.imageUrl").value(url + "/image"));
        mvc.perform(get(url)).andExpect(status().isOk()).andExpect(jsonPath("$.title").value("검은 지갑"));
        mvc.perform(signed(owner, delete(url))).andExpect(status().isNoContent());
        mvc.perform(get(url)).andExpect(status().isNotFound());
    }

    @Test
    void lostPostRequiresValidTimesAndSession() throws Exception {
        var form = multipart("/api/posts").param("kind", "lost").param("zoneLabel", "zone1")
                .param("title", "열쇠").param("description", "파란 열쇠고리")
                .param("time", "{\"lastSeenAt\":\"2026-11-19T04:00:00Z\",\"noticedMissingAt\":\"2026-11-19T03:00:00Z\"}");
        mvc.perform(form).andExpect(status().is4xxClientError());
        mvc.perform(signed(session("post-lost"), form)).andExpect(status().isBadRequest());
    }

    private AbstractMockHttpServletRequestBuilder<?> found(AbstractMockHttpServletRequestBuilder<?> form, String title) {
        return form.param("kind", "found").param("title", title).param("zoneLabel", "zone2")
                .param("time", "{\"foundAt\":\"2026-11-19T05:00:00Z\"}");
    }

    private MockHttpSession session(String subject) {
        users.save(User.fromGoogle(subject + "@example.com", subject, subject));
        var principal = new DefaultOidcUser(List.of(), OidcIdToken.withTokenValue("test").subject(subject).build());
        var authentication = UsernamePasswordAuthenticationToken.authenticated(principal, null, List.of());
        var session = new MockHttpSession();
        session.setAttribute(SPRING_SECURITY_CONTEXT_KEY, new SecurityContextImpl(authentication));
        return session;
    }

    private AbstractMockHttpServletRequestBuilder<?> signed(MockHttpSession session, AbstractMockHttpServletRequestBuilder<?> request)
            throws Exception {
        String csrf = mvc.perform(get("/csrf").session(session)).andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        String header = JsonPath.read(csrf, "$.headerName");
        String token = JsonPath.read(csrf, "$.token");
        return request.session(session).header(header, token);
    }
}
