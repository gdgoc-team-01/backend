package com.gdg.backend.application.post.dto;

import com.gdg.backend.domain.post.PostType;
import java.time.OffsetDateTime;

public record PostRequest(PostType type, String title, String zoneLabel, String description,
                          OffsetDateTime lastSeenAt, OffsetDateTime noticedMissingAt, OffsetDateTime foundAt) {
}
