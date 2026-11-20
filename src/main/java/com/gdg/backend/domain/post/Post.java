package com.gdg.backend.domain.post;

import com.gdg.backend.domain.common.BaseEntity;
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
        this.type = type;
        update(itemName, description, locationX, locationY,
                lastSeenAt, noticedMissingAt, foundAt, imageUrl);
    }

    public void update(String itemName, String description,
            BigDecimal locationX, BigDecimal locationY, OffsetDateTime lastSeenAt,
            OffsetDateTime noticedMissingAt, OffsetDateTime foundAt, String imageUrl) {
        this.itemName = itemName;
        this.description = description;
        this.locationX = locationX;
        this.locationY = locationY;
        this.lastSeenAt = lastSeenAt;
        this.noticedMissingAt = noticedMissingAt;
        this.foundAt = foundAt;
        this.imageUrl = imageUrl;
    }
}
