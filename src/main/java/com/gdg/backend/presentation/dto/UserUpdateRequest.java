package com.gdg.backend.presentation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserUpdateRequest(
        @NotBlank(message = "닉네임을 입력해주세요.")
        @Size(max = 12, message = "닉네임은 12자 이하여야 합니다.")
        String nickname
) {
}
