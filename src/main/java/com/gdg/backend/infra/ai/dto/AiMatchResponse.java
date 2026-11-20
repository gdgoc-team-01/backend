package com.gdg.backend.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record AiMatchResponse(List<Match> matches) {

    public record Match(@JsonProperty("post_id") Long postId, double score) {
    }
}
