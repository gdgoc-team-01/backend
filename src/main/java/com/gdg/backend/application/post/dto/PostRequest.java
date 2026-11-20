package com.gdg.backend.application.post.dto;

import com.gdg.backend.domain.post.PostType;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record PostRequest(
        PostType type, String itemName, String description,
        BigDecimal locationX, BigDecimal locationY,
        OffsetDateTime lastSeenAt, OffsetDateTime noticedMissingAt,
        OffsetDateTime foundAt, String imageUrl
) {
}
