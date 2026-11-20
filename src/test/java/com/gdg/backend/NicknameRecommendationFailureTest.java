package com.gdg.backend;

import com.gdg.backend.domain.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:nickname-failure-test")
@ActiveProfiles("local")
class NicknameRecommendationFailureTest {
    @MockitoBean
    private UserRepository userRepository;

    @LocalServerPort
    private int port;

    @Test
    void exhaustedRetriesReturn500() throws Exception {
        when(userRepository.existsByNickname(anyString())).thenReturn(true);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:" + port + "/users/nickname/recommend"))
                .timeout(Duration.ofSeconds(10)).GET().build();
        try (HttpClient client = HttpClient.newHttpClient()) {
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            assertEquals(500, response.statusCode());
            assertEquals("추천 닉네임을 생성하지 못했습니다.", response.body());
        }
        verify(userRepository, times(100)).existsByNickname(anyString());
        verifyNoMoreInteractions(userRepository);
    }
}
