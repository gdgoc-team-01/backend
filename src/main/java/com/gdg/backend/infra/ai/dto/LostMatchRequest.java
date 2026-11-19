package com.gdg.backend.infra.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;

public record LostMatchRequest(
        @JsonProperty("item_name") String itemName,
        String description,
        @JsonProperty("last_seen_at") OffsetDateTime lastSeenAt,
        @JsonProperty("noticed_missing_at") OffsetDateTime noticedMissingAt,
        AiLocation location
) {

    public LostMatchRequest {
        lastSeenAt = AiRequestFormat.kstMinute(lastSeenAt);
        noticedMissingAt = AiRequestFormat.kstMinute(noticedMissingAt);
    }
}
