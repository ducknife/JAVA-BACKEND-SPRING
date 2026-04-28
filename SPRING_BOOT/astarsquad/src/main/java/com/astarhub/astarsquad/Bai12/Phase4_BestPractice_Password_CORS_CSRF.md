# 🛡️ Best Practice: Password Encoding, CORS, CSRF

> **Tích hợp vào**: SecurityConfig hoàn chỉnh (File 1+2+3+4)
> **File này**: Giải thích chi tiết + tích hợp CORS config vào SecurityConfig

---

## 1. Password Encoding (BCrypt)

### 1.1 Tại Sao Phải Hash Password?

**KHÔNG BAO GIỜ** lưu password dạng plaintext hoặc hash yếu.

```
❌ Plaintext:  "password123"           → DB leak = lộ hết
❌ MD5:        "482c811da5d5b4bc..."   → Rainbow table attack (có sẵn bảng tra)
❌ SHA-256:    "ef92b778bafe77..."     → Quá nhanh → brute-force dễ
✅ BCrypt:     "$2a$12$LJ3m4..."       → Chậm có chủ đích + salt tự động
```

### 1.2 BCrypt Hoạt Động Thế Nào?

```
Input:  "myPassword123"
            │
            ▼
BCrypt(password, randomSalt, costFactor=12)
            │
            ▼
Output: "$2a$12$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy"
         │   │  │__________________________|___________________________|
         │   │  │        Salt (22 chars)    │    Hash (31 chars)        │
         │   │  └── Random mỗi lần hash                                │
         │   └── Cost factor: 2^12 = 4096 rounds                       │
         └── Algorithm version                                         │
```

**3 đặc điểm quan trọng:**

| Đặc điểm | Giải thích |
|----------|-----------|
| **Random salt** | Cùng password → hash KHÁC nhau mỗi lần. Chống rainbow table. |
| **Cost factor** | Số round tính toán. Cost 10 ≈ 100ms, 12 ≈ 400ms, 14 ≈ 1.6s. Chống brute-force. |
| **Self-contained** | Salt nằm trong hash string → không cần lưu riêng. |

### 1.3 Config — Đã Tạo Ở File 1

```java
// SecurityConfig.java (đã có từ File 1)
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(12);
    // Cost 12 = ~400ms mỗi lần hash
    // Đủ chậm để chống brute-force
    // Đủ nhanh để user không thấy delay
}

// Cách 2: DelegatingPasswordEncoder — hỗ trợ nhiều algorithm
@Bean
public PasswordEncoder passwordEncoder() {
    return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    // Default: bcrypt
    // Hỗ trợ: {bcrypt}, {scrypt}, {argon2}, {noop}
    // Hash format: {bcrypt}$2a$10$...
    // Tự detect algorithm từ prefix → dễ migrate
}
```

### 1.4 Sử Dụng — Trong UserService

```java
// ===== service/UserService.java =====
// Dùng PasswordEncoder từ SecurityConfig

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;  // Inject bean

    public UserDetails createUser(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ConflictException("Username already exists");
        }

        Role userRole = roleRepository.findByName("USER").orElseThrow();

        User user = User.builder()
            .username(request.getUsername())
            .password(passwordEncoder.encode(request.getPassword()))
            //        ^^^^^^^^^^^^^^^^^^^^^^ ENCODE trước khi lưu
            .email(request.getEmail())
            .provider(AuthProvider.LOCAL)
            .active(true)
            .roles(Set.of(userRole))
            .build();

        userRepository.save(user);
        return new CustomUserDetails(user);  // Từ File 2
    }

    public void changePassword(Long userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId).orElseThrow();

        // Kiểm tra password cũ
        if (!passwordEncoder.matches(
                request.getOldPassword(), user.getPassword())) {
            //  ^^^^^^^^^^^^^^^^^^^ matches(raw, encoded) — KHÔNG PHẢI equals()!
            throw new BadRequestException("Old password incorrect");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
```

### 1.5 Password Validation — Custom Annotation

```java
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
    String message() default "Password phải ≥8 ký tự, có chữ hoa, thường, số và ký tự đặc biệt";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class StrongPasswordValidator
        implements ConstraintValidator<StrongPassword, String> {
    @Override
    public boolean isValid(String pw, ConstraintValidatorContext ctx) {
        if (pw == null) return false;
        return pw.length() >= 8
            && pw.matches(".*[A-Z].*")
            && pw.matches(".*[a-z].*")
            && pw.matches(".*\\d.*")
            && pw.matches(".*[!@#$%^&*()].*");
    }
}

// Sử dụng trong DTO:
@Data
public class RegisterRequest {
    @NotBlank private String username;
    @StrongPassword private String password;
    @Email private String email;
}
```

---

## 2. CORS (Cross-Origin Resource Sharing)

### 2.1 CORS Là Gì?

**Same-Origin Policy** = Browser chặn request từ origin khác (bảo mật).
**CORS** = Cơ chế cho phép server chỉ định origin nào ĐƯỢC truy cập.

```
Origin = Protocol + Domain + Port
http://localhost:3000  ≠  http://localhost:8080  (khác port = khác origin)

Frontend (localhost:3000) → Backend (localhost:8080)
Browser: "Khác origin! Chặn!" → CORS Error

Giải pháp: Backend trả header cho phép:
Access-Control-Allow-Origin: http://localhost:3000
→ Browser: "OK, cho phép"
```

### 2.2 Preflight Request

```
Browser tự động gửi OPTIONS request TRƯỚC request thật (cho POST, PUT, DELETE):

── Preflight ──
OPTIONS /api/users HTTP/1.1
Origin: http://localhost:3000
Access-Control-Request-Method: POST
Access-Control-Request-Headers: Content-Type, Authorization

── Response ──
Access-Control-Allow-Origin: http://localhost:3000
Access-Control-Allow-Methods: GET, POST, PUT, DELETE
Access-Control-Allow-Headers: Content-Type, Authorization
Access-Control-Max-Age: 3600    ← Cache preflight 1 giờ

── Sau đó browser mới gửi request thật ──
POST /api/users HTTP/1.1
```

### 2.3 Tích Hợp CORS Vào SecurityConfig

```java
// ===== config/SecurityConfig.java — THÊM CORS =====

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {
    http
        // ★ THÊM MỚI — CORS config
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))

        // ... giữ nguyên csrf, session, authorizeHttpRequests
        // ... giữ nguyên oauth2ResourceServer, exceptionHandling
        ;
    return http.build();
}

@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();

    // 1. Origins được phép (KHÔNG dùng * khi có credentials)
    config.setAllowedOrigins(List.of(
        "http://localhost:3000",     // React dev
        "http://localhost:5173",     // Vite dev
        "https://yourdomain.com"    // Production
    ));

    // 2. HTTP methods được phép
    config.setAllowedMethods(List.of(
        "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"
    ));

    // 3. Headers client ĐƯỢC GỬI
    config.setAllowedHeaders(List.of(
        "Authorization",      // JWT token
        "Content-Type",       // application/json
        "X-Requested-With",
        "Accept"
    ));

    // 4. Cho phép gửi credentials (cookies, auth headers)
    config.setAllowCredentials(true);

    // 5. Cache preflight response (giây)
    config.setMaxAge(3600L);

    // 6. Headers mà browser client ĐƯỢC ĐỌC từ response
    config.setExposedHeaders(List.of(
        "Authorization",
        "X-Total-Count"       // Custom header cho pagination
    ));

    UrlBasedCorsConfigurationSource source =
        new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

### 2.4 CORS Theo Profile

```java
@Bean @Profile("dev")
public CorsConfigurationSource devCors() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*")); // Cho tất cả khi dev
    config.setAllowedMethods(List.of("*"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/**", config);
    return source;
}

@Bean @Profile("prod")
public CorsConfigurationSource prodCors() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOrigins(List.of("https://yourdomain.com"));
    config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE"));
    config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
    config.setAllowCredentials(true);

    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

---

## 3. CSRF (Cross-Site Request Forgery)

### 3.1 CSRF Là Gì?

**CSRF** = tấn công lợi dụng **browser tự động gửi cookie** khi request.

```
1. User đăng nhập bank.com → browser lưu session cookie
2. User mở evil.com (tab khác)
3. evil.com có hidden form:
   <form action="https://bank.com/transfer" method="POST">
     <input name="to" value="hacker" />
     <input name="amount" value="10000" />
   </form>
   <script>document.forms[0].submit()</script>
4. Browser TỰ ĐỘNG gửi cookie bank.com kèm request
5. bank.com nhận cookie hợp lệ → thực hiện chuyển tiền!
```

### 3.2 Khi Nào Enable/Disable?

| Kiểu App | CSRF | Lý do |
|----------|------|-------|
| **REST API + JWT (header)** | ❌ **Disable** | JWT ở Authorization header, browser KHÔNG tự gửi |
| **Server-rendered (Thymeleaf)** | ✅ Enable | Dùng session cookie, browser tự gửi |
| **SPA + Cookie-based auth** | ✅ Enable | Cookie tự gửi → cần CSRF protection |

### 3.3 Tại Sao REST API + JWT Disable CSRF An Toàn?

```
CSRF exploit: Browser tự gửi Cookie → server bị lừa
JWT:          Token ở header "Authorization: Bearer eyJ..."
              Browser KHÔNG TỰ ĐỘNG thêm header này
              → evil.com KHÔNG THỂ tự gửi JWT → AN TOÀN

Kết luận: REST API + JWT → CSRF attack KHÔNG thể xảy ra → disable OK
```

### 3.4 Config CSRF cho SPA dùng Cookie

```java
// Nếu app dùng cookie-based auth (KHÔNG phải JWT header):
http.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    // Server set cookie XSRF-TOKEN
    // SPA đọc cookie → gửi lại qua header X-XSRF-TOKEN
    // Server so sánh: cookie == header → hợp lệ
);

// Ignore CSRF cho một số path (webhook, public API):
http.csrf(csrf -> csrf
    .ignoringRequestMatchers("/api/webhooks/**")
    .ignoringRequestMatchers("/api/public/**")
);
```

---

## 4. SecurityConfig Hoàn Chỉnh — Tích Hợp Tất Cả

```java
// ===== config/SecurityConfig.java — PHIÊN BẢN CUỐI CÙNG =====
// Tích hợp: File 1 (base) + File 2 (auth) + File 3 (JWT) + File 5 (CORS/CSRF)

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtDecoder jwtDecoder;                    // File 3
    private final JwtAuthenticationConverter jwtAuthConverter; // File 3
    private final CustomAuthEntryPoint authEntryPoint;      // File 2
    private final CustomAccessDeniedHandler accessDeniedHandler; // File 2

    // Optional: OAuth2 (File 4)
    // private final CustomOAuth2UserService oAuth2UserService;
    // private final OAuth2LoginSuccessHandler oAuth2SuccessHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // ── [File 5] CORS ──
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── [File 5] CSRF — disable cho REST API + JWT ──
            .csrf(csrf -> csrf.disable())

            // ── [File 1] Session — stateless ──
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── [File 1] Authorization Rules ──
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/posts/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            // ── [File 3] JWT Resource Server ──
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthConverter)
                )
            )

            // ── [File 2] Exception Handling ──
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            );

            // ── [File 4] OAuth2 Login (optional) ──
            // .oauth2Login(oauth2 -> oauth2
            //     .userInfoEndpoint(u -> u.userService(oAuth2UserService))
            //     .successHandler(oAuth2SuccessHandler)
            // );

        return http.build();
    }

    // ── [File 1] Password ──
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    // ── [File 1] AuthenticationManager ──
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // ── [File 5] CORS Config ──
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:3000", "http://localhost:5173"));
        config.setAllowedMethods(List.of(
            "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        config.setExposedHeaders(List.of("Authorization", "X-Total-Count"));

        UrlBasedCorsConfigurationSource source =
            new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);
        return source;
    }
}
```

---

## ✅ Phase 4 — Checklist Hoàn Chỉnh

### File 1: Overview
- [ ] Hiểu Filter Chain: DelegatingFilterProxy → FilterChainProxy → SecurityFilterChain
- [ ] Hiểu SecurityContext, Authentication, GrantedAuthority

### File 2: Authentication & Authorization
- [ ] Entity RBAC: User → Role → Permission
- [ ] CustomUserDetails + CustomUserDetailsService
- [ ] @PreAuthorize (trước method), @PostAuthorize (sau method)
- [ ] Custom PermissionEvaluator hoặc PermissionService

### File 3: JWT
- [ ] JWT structure (Header, Payload, Signature)
- [ ] JWK, JwtEncoder, JwtDecoder, JwtAuthenticationConverter
- [ ] RSA (production) vs HMAC (dev)
- [ ] JwtService + AuthController (login, register, refresh)

### File 4: OAuth2
- [ ] Authorization Code Flow
- [ ] CustomOAuth2UserService + OAuth2LoginSuccessHandler
- [ ] Phân biệt OAuth2 Client vs Resource Server

### File 5: Best Practice
- [ ] BCrypt: random salt + cost factor
- [ ] CORS: preflight, allowedOrigins, credentials
- [ ] CSRF: tại sao JWT disable an toàn
