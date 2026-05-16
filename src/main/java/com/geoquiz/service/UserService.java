package com.geoquiz.service;

import com.geoquiz.model.User;
import com.geoquiz.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @PostConstruct
    public void init() {
        if (userRepository.count() == 0) {
            User user = new User();
            user.setUsername("player");
            user.setPassword(passwordEncoder.encode("p@ssword"));
            user.setRoles(Set.of("ROLE_USER"));
            userRepository.save(user);
        }
    }
}
