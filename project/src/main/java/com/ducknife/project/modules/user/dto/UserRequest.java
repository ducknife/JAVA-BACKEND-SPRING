package com.ducknife.project.modules.user.dto;

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
    private String fullName;
    @NotBlank(message = "Tên không được để trống")
    private String userName;
    @NotBlank(message = "Tên không được để trống")
    private String password;
}
