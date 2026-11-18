package com.gdg.backend.application;

import com.gdg.backend.domain.User;
import com.gdg.backend.domain.UserRepository;
import com.gdg.backend.infra.RandomNicknameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("사용자를 찾을 수 없습니다."));
    }

    public String recommendNickname() {
        RandomNicknameGenerator generator = new RandomNicknameGenerator();
        for(int attempt = 0; attempt < 100; attempt++) {
            String nickname = generator.generateRandomNickname();

            if(!userRepository.existsByNickname(nickname)) {
                return nickname;
            }
        }
        throw new NicknameGenerationException("추천 닉네임을 생성하지 못했습니다.");
    }

    public List<User> getUsers() {
        return userRepository.findAll();
    }

    public User createUser(String email, String nickname) {
        if(userRepository.existsByNickname(nickname)) {
            throw new DuplicateException("이미 존재하는 닉네임입니다.");
        }
        if(userRepository.existsByEmail(email)) {
            throw new DuplicateException("이미 존재하는 이메일입니다.");
        }

        return userRepository.save(new User(email, nickname));
    }

    public User updateUser(Long id, String nickname) {

        User user = getUser(id);

        if(userRepository.existsByNickname(nickname)) {
            throw new DuplicateException("이미 존재하는 닉네임입니다.");
        }

        user.updateNickname(nickname);

        return userRepository.save(user);
    }

    public void deleteUser(Long id) {
        User user = getUser(id);

        userRepository.deleteById(id);
    }

}
