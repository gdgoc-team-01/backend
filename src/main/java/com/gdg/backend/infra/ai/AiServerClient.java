package com.gdg.backend.infra.ai;

import com.gdg.backend.infra.ai.dto.AiEmbeddingResponse;
import com.gdg.backend.infra.ai.dto.AiMatchResponse;
import com.gdg.backend.infra.ai.dto.FoundMatchRequest;
import com.gdg.backend.infra.ai.dto.LostEmbedRequest;
import com.gdg.backend.infra.ai.dto.LostMatchRequest;
import java.time.format.DateTimeFormatter;
import java.util.Objects;
import org.springframework.http.MediaType;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.multipart.MultipartFile;

public class AiServerClient {

    private final RestClient restClient;

    public AiServerClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public AiMatchResponse matchLost(LostMatchRequest request) {
        return Objects.requireNonNull(restClient.post()
                .uri("/ai/match/lost")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiMatchResponse.class), "AI match response body is empty");
    }

    public AiMatchResponse matchFound(FoundMatchRequest request) {
        var body = new LinkedMultiValueMap<String, Object>();
        body.add("item_name", request.itemName());
        if (request.description() != null) {
            body.add("description", request.description());
        }
        body.add("found_at", request.foundAt().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        body.add("location_x", request.location().x().toPlainString());
        body.add("location_y", request.location().y().toPlainString());

        addImage(body, request.image());

        return Objects.requireNonNull(restClient.post()
                .uri("/ai/match/found")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(AiMatchResponse.class), "AI match response body is empty");
    }

    public AiEmbeddingResponse embedLost(LostEmbedRequest request) {
        return Objects.requireNonNull(restClient.post()
                .uri("/ai/embed/lost")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(AiEmbeddingResponse.class), "AI embedding response body is empty");
    }

    public AiEmbeddingResponse embedFound(MultipartFile image) {
        var body = new LinkedMultiValueMap<String, Object>();
        addImage(body, image);

        return Objects.requireNonNull(restClient.post()
                .uri("/ai/embed/found")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .accept(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(AiEmbeddingResponse.class), "AI embedding response body is empty");
    }

    private void addImage(MultiValueMap<String, Object> body, MultipartFile image) {
        String contentType = image.getContentType();
        var headers = new HttpHeaders();
        headers.setContentType(contentType == null
                        ? MediaType.APPLICATION_OCTET_STREAM
                        : MediaType.parseMediaType(contentType));
        body.add("image", new HttpEntity<>(image.getResource(), headers));
    }
}

