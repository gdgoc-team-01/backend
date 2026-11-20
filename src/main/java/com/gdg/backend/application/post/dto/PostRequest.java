package com.gdg.backend.application.post.dto;

import com.gdg.backend.domain.post.PostType;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

public record PostRequest(
        PostType type, String itemName, String description,
        BigDecimal locationX, BigDecimal locationY,
        OffsetDateTime lastSeenAt, OffsetDateTime noticedMissingAt,
        OffsetDateTime foundAt, String imageUrl
) {

    public PostRequest {
        if (type == null || itemName == null || itemName.isBlank() || itemName.length() > 255) {
            throw new IllegalArgumentException("게시글 종류와 255자 이내의 물건 이름이 필요합니다.");
        }
        if (locationX == null || locationY == null
                || locationX.signum() < 0 || locationX.compareTo(BigDecimal.valueOf(1252)) > 0
                || locationY.signum() < 0 || locationY.compareTo(BigDecimal.valueOf(1298)) > 0) {
            throw new IllegalArgumentException("지도 좌표 범위를 확인해주세요.");
        }
        if (type == PostType.LOST && (description == null || description.isBlank()
                || lastSeenAt == null || noticedMissingAt == null || foundAt != null)) {
            throw new IllegalArgumentException("분실 설명과 마지막 확인·분실 인지 시각이 필요합니다.");
        }
        if (type == PostType.FOUND && (foundAt == null || imageUrl == null || imageUrl.isBlank()
                || lastSeenAt != null || noticedMissingAt != null)) {
            throw new IllegalArgumentException("습득 시각과 이미지 URL이 필요합니다.");
        }
        if (type == PostType.LOST && lastSeenAt.isAfter(noticedMissingAt)) {
            throw new IllegalArgumentException("마지막 확인 시각은 분실 인지 시각보다 늦을 수 없습니다.");
        }
        locationX = locationX.setScale(2, RoundingMode.HALF_UP);
        locationY = locationY.setScale(2, RoundingMode.HALF_UP);
        lastSeenAt = normalize(lastSeenAt);
        noticedMissingAt = normalize(noticedMissingAt);
        foundAt = normalize(foundAt);
    }

    private static OffsetDateTime normalize(OffsetDateTime value) {
        return value == null ? null
                : value.withOffsetSameInstant(ZoneOffset.ofHours(9)).truncatedTo(ChronoUnit.MINUTES);
    }
}
