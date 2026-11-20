package com.gdg.backend.infra.outbox;

import com.gdg.backend.domain.common.BaseEntity;
import com.gdg.backend.domain.post.Post;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "embedding_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmbeddingOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_embedding_outbox_post"))
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    public EmbeddingOutbox(Post post) {
        this.post = post;
    }

    public Long getPostId() {
        return post.getId();
    }
}
