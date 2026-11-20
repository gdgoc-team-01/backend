package com.gdg.backend.application.post;

import com.gdg.backend.application.post.dto.PostRequest;
import com.gdg.backend.domain.post.Post;
import com.gdg.backend.domain.post.PostRepository;
import com.gdg.backend.domain.post.PostType;
import com.gdg.backend.infra.outbox.EmbeddingOutbox;
import com.gdg.backend.infra.outbox.EmbeddingOutboxRepository;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.context.ApplicationEventPublisher;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final EmbeddingOutboxRepository embeddingOutboxRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Post create(PostRequest request) {
        Post post = new Post(request.type(), request.itemName(), request.description(),
                request.locationX(), request.locationY(), request.lastSeenAt(),
                request.noticedMissingAt(), request.foundAt(), request.imageUrl());
        Post savedPost = postRepository.save(post);
        requestEmbedding(savedPost);
        return savedPost;
    }

    public Post get(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. id=" + id));
    }

    @Transactional
    public Post update(Long id, PostRequest request) {
        Post post = get(id);

        post.update(request.itemName(), request.description(), request.locationX(), request.locationY(),
                request.lastSeenAt(), request.noticedMissingAt(), request.foundAt(), request.imageUrl());

        requestEmbedding(post);
        return post;
    }

    private void requestEmbedding(Post post) {
        EmbeddingOutbox outbox = embeddingOutboxRepository.save(new EmbeddingOutbox(post));
        eventPublisher.publishEvent(new EmbeddingRequested(outbox.getId()));
    }

    @Transactional
    public void delete(Long id) {
        postRepository.delete(get(id));
    }
}
