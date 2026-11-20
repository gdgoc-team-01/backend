package com.gdg.backend.application.post;

import com.gdg.backend.infra.outbox.EmbeddingOutbox;
import com.gdg.backend.domain.post.Post;
import com.gdg.backend.domain.post.PostType;
import com.gdg.backend.infra.ai.AiServerClient;
import com.gdg.backend.infra.ai.dto.LostEmbedRequest;
import java.net.URI;
import java.time.Duration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
@ConditionalOnProperty(prefix = "ai.server", name = "base-url")
public class EmbeddingEventListener {

    private final EmbeddingOutboxHandler embeddingOutboxHandler;
    private final AiServerClient aiClient;
    private final RestClient imageClient;

    public EmbeddingEventListener(EmbeddingOutboxHandler embeddingOutboxHandler, AiServerClient aiClient) {
        this.embeddingOutboxHandler = embeddingOutboxHandler;
        this.aiClient = aiClient;

        var factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3));
        factory.setReadTimeout(Duration.ofSeconds(30));

        this.imageClient = RestClient.builder().requestFactory(factory).build();
    }

    @Async("embeddingExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onEmbeddingRequested(EmbeddingRequested event) {
        embeddingOutboxHandler.find(event.outboxId()).ifPresent(this::process);
    }

    private void process(EmbeddingOutbox task) {
        Post post = embeddingOutboxHandler.findPost(task.getPostId());
        if (post.getType() == PostType.LOST) {
            embedText(post);
        }
        if (post.getType() == PostType.FOUND) {
            embedImage(post);
        }

        embeddingOutboxHandler.delete(task.getId());
    }

    private void embedText(Post post) {
        aiClient.embedLost(new LostEmbedRequest(
                post.getId(), post.getItemName(), post.getDescription()));
    }

    private void embedImage(Post post) {
        URI uri = URI.create(post.getImageUrl());
        var response = imageClient.get().uri(uri).retrieve().toEntity(byte[].class);
        byte[] bytes = response.getBody();
        var image = new ByteArrayResource(bytes) {
            @Override
            public String getFilename() {
                return "image";
            }
        };
        MediaType contentType = response.getHeaders().getContentType();
        aiClient.embedFound(post.getId(), image, contentType);
    }
}
