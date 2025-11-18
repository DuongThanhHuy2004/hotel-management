package com.hotel.service;

import com.hotel.entity.User;
import com.hotel.entity.Role;
import com.hotel.repository.RoleRepository;
import com.hotel.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import com.hotel.enums.Provider; // Đảm bảo import đúng

@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauthUser = super.loadUser(userRequest);

        // Chỉ xử lý Google (đã xóa logic Facebook)
        processOAuthUser(oauthUser);

        return oauthUser;
    }

    private void processOAuthUser(OAuth2User oauthUser) {
        Map<String, Object> attributes = oauthUser.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String providerId = oauthUser.getName(); // ID duy nhất của Google
        Provider provider = Provider.GOOGLE; // Chỉ có Google

        // Logic lưu DB (giữ nguyên)
        Optional<User> existUser = userRepository.findByUsername(email);

        if (existUser.isEmpty()) {
            User newUser = new User();
            newUser.setUsername(email);
            newUser.setEmail(email);
            newUser.setFullName(name);
            newUser.setProvider(provider);
            newUser.setProviderId(providerId);
            newUser.setPassword(null);

            Role userRole = roleRepository.findByName("ROLE_USER")
                    .orElseThrow(() -> new RuntimeException("Error: Role 'ROLE_USER' is not found."));
            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            newUser.setRoles(roles);

            userRepository.save(newUser);
        } else {
            User user = existUser.get();
            if (user.getProvider() == Provider.LOCAL) {
                user.setProvider(provider);
                user.setProviderId(providerId);
                user.setFullName(name);
                userRepository.save(user);
            }
        }
    }
}