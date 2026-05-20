package com.geoquiz.security;

import com.geoquiz.model.User;
import com.geoquiz.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration().getRegistrationId().toUpperCase();
        String providerId = oAuth2User.getAttribute("sub"); // Google uses "sub" for user ID
        String email = oAuth2User.getAttribute("email");
        if (email == null) {
            email = providerId + "@" + provider.toLowerCase() + ".local";
        }


        Optional<User> userOptional = userRepository.findByEmail(email);
        User user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            if (user.getProvider() == null) {
                user.setProvider(provider);
                user.setProviderId(providerId);
                user = userRepository.save(user);
            }
        } else {
            user = new User();
            user.setEmail(email);
            // Use email as username to ensure uniqueness, as names can clash
            user.setUsername(email);
            user.setProvider(provider);
            user.setProviderId(providerId);
            user.setEnabled(true);
            user = userRepository.save(user);
        }

        return new CustomUserDetails(user, oAuth2User.getAttributes());
    }
}
