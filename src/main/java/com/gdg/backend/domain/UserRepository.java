package com.gdg.backend.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long>{

    boolean existsByEmail(String email);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByNickname(String nickname);
    Optional<User> findByGoogleSubject(String googleSubject);
}
