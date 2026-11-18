package com.gdg.backend.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LostEmbedRequest(
        @JsonProperty("item_name") String itemName,
        @JsonProperty("item_features") String itemFeatures
) {
}
