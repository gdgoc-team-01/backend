package com.gdg.backend.domain.post;

import com.gdg.backend.domain.common.BaseEntity;
import com.gdg.backend.domain.DomainException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Post extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PostType type;

    @Column(nullable = false)
    private String itemName;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal locationX;

    @Column(nullable = false, precision = 6, scale = 2)
    private BigDecimal locationY;

    private OffsetDateTime lastSeenAt;

    private OffsetDateTime noticedMissingAt;

    private OffsetDateTime foundAt;

    @Column(columnDefinition = "text")
    private String imageUrl;

    public Post(PostType type, String itemName, String description,
            BigDecimal locationX, BigDecimal locationY, OffsetDateTime lastSeenAt,
            OffsetDateTime noticedMissingAt, OffsetDateTime foundAt, String imageUrl) {
        validate(type, itemName, description, locationX, locationY, lastSeenAt, noticedMissingAt, imageUrl);

        this.type = type;
        this.itemName = itemName;
        this.description = description;
        this.locationX = locationX;
        this.locationY = locationY;
        this.lastSeenAt = lastSeenAt;
        this.noticedMissingAt = noticedMissingAt;
        this.foundAt = foundAt;
        this.imageUrl = imageUrl;
    }

    public void update(String itemName, String description,
            BigDecimal locationX, BigDecimal locationY, OffsetDateTime lastSeenAt,
            OffsetDateTime noticedMissingAt, OffsetDateTime foundAt, String imageUrl) {
        validate(type, itemName, description, locationX, locationY, lastSeenAt, noticedMissingAt, imageUrl);

        this.itemName = itemName;
        this.description = description;
        this.locationX = locationX;
        this.locationY = locationY;
        this.lastSeenAt = lastSeenAt;
        this.noticedMissingAt = noticedMissingAt;
        this.foundAt = foundAt;
        this.imageUrl = imageUrl;
    }

    private void validate(PostType type, String itemName, String description,
            BigDecimal locationX, BigDecimal locationY, OffsetDateTime lastSeenAt,
            OffsetDateTime noticedMissingAt, String imageUrl) {
        if (itemName.isBlank() || itemName.length() > 255) {
            throw new DomainException("물건 이름은 비어 있을 수 없으며 255자 이내여야 합니다.");
        }
        if (locationX.signum() < 0 || locationX.compareTo(BigDecimal.valueOf(1252)) > 0
                || locationY.signum() < 0 || locationY.compareTo(BigDecimal.valueOf(1298)) > 0) {
            throw new DomainException("지도 좌표 범위를 확인해주세요.");
        }
        if (type == PostType.LOST) {
            if (description.isBlank()) {
                throw new DomainException("분실물 설명을 입력해주세요.");
            }
            if (lastSeenAt.isAfter(noticedMissingAt)) {
                throw new DomainException("마지막 확인 시각은 분실 인지 시각보다 늦을 수 없습니다.");
            }
        }
        if (type == PostType.FOUND && imageUrl.isBlank()) {
            throw new DomainException("습득 이미지 URL을 입력해주세요.");
        }
    }
}
