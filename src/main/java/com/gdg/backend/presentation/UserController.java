package com.gdg.backend.presentation;

import com.gdg.backend.application.GoogleAccountService;
import com.gdg.backend.application.UserService;
import com.gdg.backend.domain.User;
import com.gdg.backend.presentation.dto.NicknameRecommendResponse;
import com.gdg.backend.presentation.dto.UserResponse;
import com.gdg.backend.presentation.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final GoogleAccountService accounts;

    @GetMapping("/nickname/recommend")
    public NicknameRecommendResponse recommendNickname() {
        String nickname = userService.recommendNickname();
        return new NicknameRecommendResponse(nickname);
    }

    @GetMapping("/{id}")
    public UserResponse getUser(@PathVariable Long id) {
        User user = userService.getUser(id);
        return new UserResponse(user.getId(), user.getNickname());
    }

    @PatchMapping("/{id}")
    public UserResponse updateUser(
            @PathVariable Long id,
            @AuthenticationPrincipal OidcUser principal,
            @Valid @RequestBody UserUpdateRequest request) {
        requireOwner(id, principal);
        User user = userService.updateUser(id, request.nickname());
        return new UserResponse(user.getId(), user.getNickname());
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id, @AuthenticationPrincipal OidcUser principal) {
        requireOwner(id, principal);
        userService.deleteUser(id);
    }

    private void requireOwner(Long id, OidcUser principal) {
        if (principal == null || !accounts.getUser(principal.getSubject()).getId().equals(id)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);
        }
    }
}
