package com.gdg.backend.domain;

public enum ErrorCode {

    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    NICKNAME_DUPLICATED("이미 존재하는 닉네임입니다."),
    EMAIL_DUPLICATED("이미 존재하는 이메일입니다."),
    NICKNAME_BLANK("닉네임을 입력해주세요."),
    NICKNAME_TOO_LONG("닉네임은 12자 이하로 입력해주세요."),
    INVALID_REQUEST("잘못된 요청입니다."),
    NICKNAME_GENERATION_FAILED("추천 닉네임을 생성하지 못했습니다."),
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다.");

    private final String message;

    ErrorCode(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
