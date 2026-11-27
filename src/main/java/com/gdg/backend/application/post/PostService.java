package com.gdg.backend.application.post;

import com.gdg.backend.application.post.dto.PostRequest;
import com.gdg.backend.domain.post.Post;
import com.gdg.backend.domain.post.PostRepository;
import com.gdg.backend.domain.post.PostType;
import com.gdg.backend.infra.outbox.EmbeddingOutbox;
import com.gdg.backend.infra.outbox.EmbeddingOutboxRepository;
import java.util.List;
import com.gdg.backend.application.NotFoundException;
import com.gdg.backend.domain.DomainException;
import com.gdg.backend.domain.post.PostImage;
import org.springframework.security.access.AccessDeniedException;

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
    public Post create(Long authorId, PostRequest request, PostImage image) {
        Post post = new Post(authorId, request.type());
        edit(post, request, image);
        postRepository.save(post);
        requestEmbedding(post);
        return post;
    }

    public List<Post> list(PostType type, String zone, Sort sort) {
        return postRepository.search(type, zone, sort);
    }

    public Post get(Long id) {
        return postRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다. id=" + id));
    }

    public PostImage image(Long id) {
        return postRepository.findWithImageById(id).map(Post::getImage)
                .orElseThrow(() -> new NotFoundException("이미지를 찾을 수 없습니다."));
    }

    @Transactional
    public Post update(Long id, Long authorId, PostRequest request, PostImage image) {
        Post post = ownedPost(id, authorId);
        if (post.getType() != request.type()) {
            throw new DomainException("게시글 종류는 변경할 수 없습니다.");
        }
        edit(post, request, image);
        requestEmbedding(post);
        return post;
    }

    private void edit(Post post, PostRequest request, PostImage image) {
        post.edit(request.title(), request.zoneLabel(), request.description(),
                request.lastSeenAt(), request.noticedMissingAt(), request.foundAt(), image);
    }

    private Post ownedPost(Long id, Long authorId) {
        Post post = get(id);
        if (!authorId.equals(post.getAuthorId())) {
            throw new AccessDeniedException("작성자만 게시글을 변경할 수 있습니다.");
        }
        return post;
    }

    private void requestEmbedding(Post post) {
        EmbeddingOutbox outbox = embeddingOutboxRepository.save(new EmbeddingOutbox(post));
        eventPublisher.publishEvent(new EmbeddingRequested(outbox.getId()));
    }

    @Transactional
    public void delete(Long id, Long authorId) {
        postRepository.delete(ownedPost(id, authorId));
    }
}
