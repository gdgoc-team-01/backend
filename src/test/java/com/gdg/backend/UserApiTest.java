package com.gdg.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.gdg.backend.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.gdg.backend.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
public class UserApiTest {

    @Autowired
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @Test
    void duplicateNicknameReturns409() throws Exception {

        User firstUser = userRepository.save(
                new User("first@example.com", "행복한 수달")
        );

        User secondUser = userRepository.save(
                new User("second@example.com", "졸린 고양이")
        );

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/users/" + firstUser.getId()))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .method("PATCH", HttpRequest.BodyPublishers.ofString(
                        "{\"nickname\":\"졸린 고양이\"}"
                ))
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(
                    request, HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(409, response.statusCode());
            assertEquals("이미 존재하는 닉네임입니다.", response.body());
            assertEquals("행복한 수달",
                    userRepository.findById(firstUser.getId()).orElseThrow().getNickname());
            assertEquals("졸린 고양이",
                    userRepository.findById(secondUser.getId()).orElseThrow().getNickname());
        } finally {
            userRepository.deleteAllById(java.util.List.of(firstUser.getId(), secondUser.getId()));
        }
    }
}
