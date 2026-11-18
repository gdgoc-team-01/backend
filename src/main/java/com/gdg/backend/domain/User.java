package com.gdg.backend.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Column;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Column(nullable = false, unique = true)
    private String email;

    public User(String email, String nickname) {
        validateNickname(nickname);

        this.email = email;
        this.nickname = nickname;
    }

    public void updateNickname(String nickname) {
        validateNickname(nickname);

        this.nickname = nickname;
    }

    private void validateNickname(String nickname) {
        if(nickname.length() > 12) {
            throw new DomainException("닉네임은 12자 이하여야 합니다.");
        }

        if(nickname.isBlank()) {
            throw new DomainException("닉네임을 입력해주세요.");
        }
    }

    public String getEmail() {
        return this.email;
    }

    public String getNickname() {
        return this.nickname;
    }

    public Long getId() {
        return this.id;
    }
}
