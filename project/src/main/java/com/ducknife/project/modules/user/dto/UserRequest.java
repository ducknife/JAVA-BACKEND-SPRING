package com.ducknife.project.modules.user.dto;

import java.util.HashSet;
import java.util.Set;

import com.ducknife.project.modules.role.Role;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserRequest {
    @NotBlank(message = "Tên không được để trống")
    private String fullname;
    @NotBlank(message = "Tên không được để trống")
    private String username;
    @NotBlank(message = "Tên không được để trống")
    private String password;
    private Set<String> roles;
}
