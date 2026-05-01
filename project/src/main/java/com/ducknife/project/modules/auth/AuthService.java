package com.ducknife.project.modules.auth;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ducknife.project.common.exception.UnauthorizedException;
import com.ducknife.project.config.properties.JwtProperties;
import com.ducknife.project.modules.auth.dto.AuthResponse;
import com.ducknife.project.modules.auth.dto.LoginRequest;
import com.ducknife.project.modules.user.UserRepository;
import com.ducknife.project.security.CustomUserDetails;
import com.ducknife.project.security.JwtService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {
    private final AuthenticationManager authManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProps;

    @Transactional
    public AuthResponse checkLogin(LoginRequest request) {
        String username = request.getUsername();
        String password = request.getPassword();

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, password));

        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(userDetails);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .expiresIn(jwtProps.getAccessTokenExpiration())
                .build();
    }
}
