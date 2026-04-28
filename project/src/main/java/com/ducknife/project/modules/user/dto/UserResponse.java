package com.ducknife.project.modules.user.dto;

import java.util.Set;
import java.util.stream.Collectors;

import com.ducknife.project.modules.role.Role;
import com.ducknife.project.modules.user.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {
    private Long userId;
    private String fullname;
    private String username;
    private String email;
    private Set<String> roles;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .userId(user.getId())
                .fullname(user.getFullname())
                .username(user.getUsername())
                .roles(user.getRoles()
                        .stream()
                        .map(r -> r.getName())
                        .collect(Collectors.toSet()))
                .build();
    }
}
