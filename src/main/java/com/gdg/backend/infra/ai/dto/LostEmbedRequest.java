package com.gdg.backend.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LostEmbedRequest(
        @JsonProperty("post_id") Long postId,
        @JsonProperty("item_name") String itemName,
        String description
) {
}
