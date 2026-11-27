package com.gdg.backend.presentation;

import com.gdg.backend.application.GoogleAccountService;
import com.gdg.backend.application.post.PostService;
import com.gdg.backend.application.post.dto.PostRequest;
import com.gdg.backend.domain.DomainException;
import com.gdg.backend.domain.post.Post;
import com.gdg.backend.domain.post.PostImage;
import com.gdg.backend.domain.post.PostType;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.IOException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/posts")
@RequiredArgsConstructor
public class PostController {
    private final PostService posts;
    private final GoogleAccountService accounts;
    private final ObjectMapper mapper;

    @GetMapping
    List<PostView> list(@RequestParam(required = false) String kind,
                        @RequestParam(required = false) String zoneLabel,
                        @RequestParam(defaultValue = "newest") String sort) {
        if (zoneLabel != null && !zoneLabel.matches("zone[1-5]")) {
            throw new DomainException("올바른 구역을 선택해주세요.");
        }
        Sort.Direction direction = switch (sort) {
            case "newest" -> Sort.Direction.DESC;
            case "oldest" -> Sort.Direction.ASC;
            default -> throw new DomainException("정렬은 newest 또는 oldest를 사용해주세요.");
        };
        return posts.list(kind == null ? null : parseKind(kind), zoneLabel, Sort.by(direction, "createdAt", "id"))
                .stream().map(PostView::from).toList();
    }

    @GetMapping("/{id}")
    PostView get(@PathVariable Long id) {
        return PostView.from(posts.get(id));
    }

    @GetMapping("/{id}/image")
    ResponseEntity<byte[]> image(@PathVariable Long id) {
        PostImage image = posts.image(id);
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(image.getMediaType()))
                .header("X-Content-Type-Options", "nosniff").body(image.getData());
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    PostView create(@AuthenticationPrincipal OidcUser user, @ModelAttribute PostForm form) throws IOException {
        return PostView.from(posts.create(accounts.getUser(user.getSubject()).getId(),
                request(form), form.upload()));
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    PostView update(@AuthenticationPrincipal OidcUser user, @PathVariable Long id,
                    @ModelAttribute PostForm form) throws IOException {
        return PostView.from(posts.update(id, accounts.getUser(user.getSubject()).getId(),
                request(form), form.upload()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void delete(@AuthenticationPrincipal OidcUser user, @PathVariable Long id) {
        posts.delete(id, accounts.getUser(user.getSubject()).getId());
    }

    private PostRequest request(PostForm form) {
        PostType type = parseKind(form.kind());
        Times times;
        try {
            times = form.time() == null ? null : mapper.readValue(form.time(), Times.class);
        } catch (JacksonException e) {
            throw new DomainException("time에 ISO 8601 시각을 담은 JSON 객체를 입력해주세요.");
        }
        if (times == null) {
            throw new DomainException("시간 정보를 입력해주세요.");
        }
        return new PostRequest(type, form.title(), form.zoneLabel(), form.description(),
                times.lastSeenAt(), times.noticedMissingAt(), times.foundAt());
    }

    private static PostType parseKind(String kind) {
        if ("lost".equals(kind)) return PostType.LOST;
        if ("found".equals(kind)) return PostType.FOUND;
        throw new DomainException("kind는 lost 또는 found여야 합니다.");
    }

    public record PostForm(String kind, String title, String zoneLabel, String description,
                           String time, MultipartFile image) {
        PostImage upload() throws IOException {
            return image == null ? null : new PostImage(image.getBytes());
        }
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    record Times(OffsetDateTime lastSeenAt, OffsetDateTime noticedMissingAt, OffsetDateTime foundAt) {}

    record PostView(String id, String kind, String title, String zoneLabel, String description,
                    Instant createdAt, Times time, String imageUrl) {
        static PostView from(Post post) {
            Times time = post.getType() == PostType.LOST
                    ? new Times(post.getLastSeenAt(), post.getNoticedMissingAt(), null)
                    : new Times(null, null, post.getFoundAt());
            String image = post.getImage() == null ? post.getImageUrl() : "/api/posts/" + post.getId() + "/image";
            return new PostView(post.getId().toString(), post.getType().name().toLowerCase(Locale.ROOT),
                    post.getItemName(), post.getZoneLabel(), post.getDescription(), post.getCreatedAt().toInstant(),
                    time, image);
        }
    }
}
