package com.ducknife.project.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ducknife.project.config.properties.AdminBootstrapProperties;
import com.ducknife.project.modules.user.User;
import com.ducknife.project.modules.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {
    private final AdminBootstrapProperties adminProps;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.existsByRolesName("ROLE_ADMIN")) {
            return;
        }
        User user = User.builder()
                .fullname(adminProps.getFullname())
                .username(adminProps.getUsername())
                .password(passwordEncoder.encode(adminProps.getPassword()))
                .email(adminProps.getEmail())
                .build();
        userRepository.save(user);
    }
}
