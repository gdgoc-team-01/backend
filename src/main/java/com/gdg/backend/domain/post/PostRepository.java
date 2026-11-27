package com.gdg.backend.domain.post;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long> {

    @Query("""
            select p from Post p where (:type is null or p.type = :type)
            and (:zone is null or p.zoneLabel = :zone)
            """)
    List<Post> search(PostType type, String zone, Sort sort);

    @EntityGraph(attributePaths = "image")
    Optional<Post> findWithImageById(Long id);
}
