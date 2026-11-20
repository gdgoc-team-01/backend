package com.gdg.backend.infra;

import koreannickname.KoreanNicknameGenerator;

public class RandomNicknameGenerator {

    private final KoreanNicknameGenerator generator = KoreanNicknameGenerator.create();

    public String generateRandomNickname() {
        return generator.generate();
    }
}
