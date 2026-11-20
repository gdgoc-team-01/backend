package com.gdg.backend.presentation;

import com.gdg.backend.application.UserService;
import com.gdg.backend.domain.User;
import com.gdg.backend.presentation.dto.NicknameRecommendResponse;
import com.gdg.backend.presentation.dto.UserResponse;
import com.gdg.backend.presentation.dto.UserUpdateRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

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
            @Valid @RequestBody UserUpdateRequest request) {
        User user = userService.updateUser(id, request.nickname());
        return new UserResponse(user.getId(), user.getNickname());
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }
}
