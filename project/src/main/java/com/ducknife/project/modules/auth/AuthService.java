package com.ducknife.project.modules.auth;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.auth.dto.LoginRequest;
import com.ducknife.project.modules.user.User;
import com.ducknife.project.modules.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(rollbackFor = Exception.class)
    public String authenticate(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng!"));
        if (passwordEncoder.matches(password, user.getPassword())) {
            return "Xác thực thành công";
        }
        return "Lỗi xác thực! Vui lòng đăng nhập lại!";
    }
}
