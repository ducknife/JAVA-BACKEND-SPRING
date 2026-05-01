package com.ducknife.project.config;

import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.config.properties.AdminBootstrapProperties;
import com.ducknife.project.modules.role.Role;
import com.ducknife.project.modules.role.RoleRepository;
import com.ducknife.project.modules.user.User;
import com.ducknife.project.modules.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class AdminBootstrapRunner implements CommandLineRunner {
    private final AdminBootstrapProperties adminProps;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername(adminProps.getUsername())
                || userRepository.existsByRolesName("ROLE_ADMIN")) {
            return;
        }
        User user = User.builder()
                .fullname(adminProps.getFullname())
                .username(adminProps.getUsername())
                .password(passwordEncoder.encode(adminProps.getPassword()))
                .email(adminProps.getEmail())
                .roles(adminProps.getRoles().stream()
                        .map(rn -> {
                            Role role = roleRepository.findByName(rn)
                                    .orElseThrow(() -> new ResourceNotFoundException("Role not found"));
                            return role;
                        })
                        .collect(Collectors.toSet()))
                .build();
        userRepository.save(user);
    }
}
