package com.gdg.backend;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.gdg.backend.domain.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import com.gdg.backend.domain.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.Arguments;
import java.util.stream.Stream;
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
        User user = userRepository.save(new User("validation@example.com", "행복한 수달"));

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/users/" + user.getId()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(400, response.statusCode());
            assertEquals(expectedMessage, response.body());
            assertEquals("행복한 수달",
                    userRepository.findById(user.getId()).orElseThrow().getNickname());
        } finally {
            userRepository.deleteById(user.getId());
        }
    }

    @Test
    void twelveCharacterNicknameIsAccepted() throws Exception {
        User user = userRepository.save(new User("boundary@example.com", "행복한 수달"));
        String nickname = "가나다라마바사아자차카타";

        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("http://localhost:" + port + "/users/" + user.getId()))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(
                            "{\"nickname\":\"" + nickname + "\"}"))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            assertEquals(200, response.statusCode());
            assertEquals(nickname,
                    userRepository.findById(user.getId()).orElseThrow().getNickname());
        } finally {
            userRepository.deleteById(user.getId());
        }
    }

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
