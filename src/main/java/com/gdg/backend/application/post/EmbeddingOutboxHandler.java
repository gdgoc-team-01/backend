package com.gdg.backend.application.post;

import com.gdg.backend.infra.outbox.EmbeddingOutbox;
import com.gdg.backend.infra.outbox.EmbeddingOutboxRepository;
import com.gdg.backend.domain.post.Post;
import com.gdg.backend.domain.post.PostRepository;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EmbeddingOutboxHandler {

    private final EmbeddingOutboxRepository repository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Post findPost(Long postId) {
        return postRepository.findWithImageById(postId)
                .orElseThrow(() -> new NoSuchElementException("게시글을 찾을 수 없습니다. id=" + postId));
    }

    @Transactional(readOnly = true, propagation = Propagation.REQUIRES_NEW)
    public Optional<EmbeddingOutbox> find(Long id) {
        return repository.findById(id);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void delete(Long id) {
        repository.deleteById(id);
    }
}
