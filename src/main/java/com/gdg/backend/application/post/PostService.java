package com.gdg.backend.application.post;

import com.gdg.backend.application.post.dto.PostRequest;
import com.gdg.backend.domain.post.Post;
import com.gdg.backend.domain.post.PostRepository;
import com.gdg.backend.domain.post.PostType;
import java.util.List;
import java.util.NoSuchElementException;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;

    @Transactional
    public Post create(PostRequest request) {
        Post post = new Post(request.type(), request.itemName(), request.description(),
                request.locationX(), request.locationY(), request.lastSeenAt(),
                request.noticedMissingAt(), request.foundAt(), request.imageUrl());
        return postRepository.save(post);
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

        return post;
    }

    @Transactional
    public void delete(Long id) {
        postRepository.delete(get(id));
    }
}
