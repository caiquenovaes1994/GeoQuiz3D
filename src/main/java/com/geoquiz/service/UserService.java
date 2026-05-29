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
            user.setEmail("player@geoquiz.local");
            user.setPassword(passwordEncoder.encode("p@ssword"));
            user.setRoles(Set.of("ROLE_USER"));
            userRepository.save(user);
        }
    }

    public void registerUser(String email, String password) throws IllegalArgumentException {
        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("E-mail já está em uso.");
        }
        
        // Verifica se o username gerado a partir do email também existe
        String generatedUsername = email.split("@")[0];
        if (userRepository.findByUsername(generatedUsername).isPresent()) {
            // Se existir, usa o e-mail completo como username para evitar colisão
            generatedUsername = email;
        }

        User newUser = new User();
        newUser.setUsername(generatedUsername);
        newUser.setEmail(email);
        newUser.setPassword(passwordEncoder.encode(password));
        newUser.setProvider("LOCAL");
        newUser.setEnabled(true);
        newUser.setRoles(Set.of("ROLE_USER"));
        
        userRepository.save(newUser);
    }
}
