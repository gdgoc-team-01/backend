package com.gdg.backend.infra.ai.dto;

import java.time.OffsetDateTime;
import org.springframework.web.multipart.MultipartFile;

public record FoundMatchRequest(
        String itemName,
        String description,
        MultipartFile image,
        OffsetDateTime foundAt,
        AiLocation location
) {
    public FoundMatchRequest {
        foundAt = AiRequestFormat.kstMinute(foundAt);
    }
}
