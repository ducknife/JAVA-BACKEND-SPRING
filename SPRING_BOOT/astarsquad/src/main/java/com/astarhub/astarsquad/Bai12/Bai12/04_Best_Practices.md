# Bài 12.4: Best Practices Bảo Mật Trong Spring Boot

> **Stack**: Spring Boot 3.4.3 · Spring Security 6.4.x · Java 17+

## 1. Password Encoding với BCrypt

### Tại Sao Không Lưu Plain Text Password?

```
❌ Sai:  password = "123456"          (plain text — cực kỳ nguy hiểm)
❌ Sai:  password = MD5("123456")     (MD5 đã bị crack)
❌ Sai:  password = SHA1("123456")    (không có salt → rainbow table)
✅ Đúng: password = BCrypt("123456")  (có salt, chậm có chủ ý, an toàn)
```

### BCrypt Hoạt Động Như Thế Nào?

```
Input:  "myPassword"
         │
         ▼
BCrypt("myPassword", salt=random, rounds=10)
         │
         ▼
Output: "$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJXQe/gH9ZW"
          │   │   │
          │   │   └── Hash (53 chars)
          │   └────── Cost factor (2^10 = 1024 iterations)
          └────────── Algorithm version
```

> Mỗi lần hash cho ra kết quả **khác nhau** (do salt ngẫu nhiên), nhưng `matches()` vẫn so sánh đúng.

### Triển Khai

```java
// Cấu hình Bean
@Bean
public PasswordEncoder passwordEncoder() {
    // 10 là cost factor (mặc định và khuyên dùng)
    // Tăng lên để tăng bảo mật nhưng giảm hiệu năng
    return new BCryptPasswordEncoder(10);
}

// Đăng ký - mã hóa password
@Service
@RequiredArgsConstructor
public class AuthService {

    private final PasswordEncoder passwordEncoder;
    private final UserRepository userRepository;

    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username đã tồn tại");
        }

        User user = new User();
        user.setUsername(request.getUsername());

        // Luôn encode password trước khi lưu
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        return userRepository.save(user);
    }
}

// Kiểm tra password - Spring Security tự làm việc này
// Nhưng nếu cần tự check:
boolean isMatch = passwordEncoder.matches(rawPassword, encodedPassword);
```

### Không Bao Giờ:
```java
// ❌ KHÔNG làm thế này
user.setPassword(request.getPassword()); // plain text

// ❌ KHÔNG so sánh trực tiếp
if (user.getPassword().equals(inputPassword)) { ... }

// ❌ KHÔNG decode password
String raw = BCrypt.decode(user.getPassword()); // không thể!
```

---

## 2. CORS Configuration

### CORS Là Gì?

**CORS (Cross-Origin Resource Sharing)** là cơ chế bảo mật của trình duyệt ngăn chặn trang web gửi request đến domain khác không được phép.

```
Frontend (localhost:3000)
    │
    │ fetch("http://localhost:8080/api/products")
    │
    ▼
Browser kiểm tra: "Origin localhost:3000 có được phép không?"
    │
    ├── Được phép → Gửi request
    └── Không phép → Blocked! CORS Error
```

### Cấu Hình CORS — @ConfigurationProperties (chuẩn hơn)

> **Senior note**: Dùng `@ConfigurationProperties` thay vì hardcode List.of() trong code. Có thể đổi origin không cần recompile.

```yaml
# application-dev.yml
app:
  cors:
    allowed-origins:
      - http://localhost:3000
      - http://localhost:5173
    allowed-methods: GET,POST,PUT,DELETE,PATCH,OPTIONS
    max-age: 3600

# application-prod.yml
app:
  cors:
    allowed-origins:
      - https://yourapp.com
      - https://www.yourapp.com
    max-age: 86400
```

```java
// config/CorsProperties.java
@ConfigurationProperties(prefix = "app.cors")
@Validated
public record CorsProperties(
    @NotEmpty List<String> allowedOrigins,
    @NotBlank String allowedMethods,
    long maxAge
) {}

// config/CorsConfig.java
@Configuration
@RequiredArgsConstructor
public class CorsConfig {

    private final CorsProperties corsProperties;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();

        // ── Từ @ConfigurationProperties — không hardcode ──
        config.setAllowedOrigins(corsProperties.allowedOrigins());
        config.setAllowedMethods(
            Arrays.asList(corsProperties.allowedMethods().split(",")));
        config.setAllowedHeaders(List.of(
            "Authorization", "Content-Type", "Accept", "X-Requested-With"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);   // cần true khi dùng cookie
        config.setMaxAge(corsProperties.maxAge());

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### Tích Hợp Vào SecurityConfig

```java
// Cấu hình trong SecurityConfig.java — inject CorsConfigurationSource bean
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .cors(cors -> cors.configurationSource(corsConfigurationSource))
        .csrf(csrf -> csrf.disable())
        // ...
        .build();
}
```

> ⚠️ **Không** dùng `allowedOriginPatterns("*")` trong production. Chỉ dùng trong môi trường dev (local).

---

## 3. CSRF Protection

### CSRF Là Gì?

**CSRF (Cross-Site Request Forgery)** là tấn công lừa người dùng thực hiện hành động không mong muốn trên website họ đang đăng nhập.

**Kịch bản tấn công:**
```
1. Bạn đang đăng nhập banking.com (có session cookie)
2. Bạn vào trang evil.com
3. evil.com có hidden form: POST banking.com/transfer?to=hacker&amount=10000000
4. Browser tự gửi request kèm cookie của banking.com
5. Tiền bị chuyển mà bạn không hay biết!
```

### REST API Với JWT: Tắt CSRF

```java
// JWT là stateless → không cần CSRF token
// (Attacker không thể lấy JWT từ cookie của trang khác)
http.csrf(csrf -> csrf.disable());
```

> ✅ **OK** khi dùng JWT trong `Authorization: Bearer` header
> ❌ **KHÔNG OK** nếu dùng cookie để lưu JWT

### Web App Truyền Thống: Bật CSRF

```java
// Bật CSRF (mặc định trong Spring Security)
http.csrf(csrf -> csrf
    .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
    .ignoringRequestMatchers("/api/public/**") // Bỏ qua public endpoints
);
```

### Bật CSRF Chọn Lọc (REST API + một số form)

```java
http.csrf(csrf -> csrf
    .ignoringRequestMatchers(
        "/api/auth/**",     // Login/register không cần
        "/api/webhook/**"   // Webhook từ bên ngoài
    )
);
```

---

## 4. Các Best Practices Khác

### 4.1 DTO Projection — Không Lộ Thông Tin Nhạy Cảm

```java
// ❌ Trả về entity thẳng (có chứa password hash)
@GetMapping("/users/{id}")
public User getUser(@PathVariable Long id) {
    return userRepository.findById(id).orElseThrow();
}

// ✅ Dùng Java record (DTO projection) — chỉ trả những gì cần
public record UserResponse(
    Long id,
    String username,
    String email,
    Set<String> roles
    // KHÔNG có password, providerId, imageUrl...
) {
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRoles().stream()
                .map(r -> r.getName().name()).collect(Collectors.toSet())
        );
    }
}

@GetMapping("/users/{id}")
public ResponseEntity<UserResponse> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(
        UserResponse.from(
            userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User " + id + " not found"))
        )
    );
}
```

### 4.2 Validate Input — Dùng Record + @Valid

```java
// DTO dùng record — Java 16+
public record RegisterRequest(
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username 3-50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
             message = "Username chỉ chứa chữ, số và gạch dưới")
    String username,

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password tối thiểu 8 ký tự")
    // Khuyên nên thêm điều kiện: 1 hoa, 1 thường, 1 số, 1 special char
    String password,

    @Email(message = "Email không hợp lệ")
    @NotBlank
    String email,

    Set<String> roles
) {}

// Controller — chỉ cần @Valid, lỗi được GlobalExceptionHandler xử lý
@PostMapping("/register")
@ResponseStatus(HttpStatus.CREATED)
public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
    return authService.register(request);
    // KHAI Tử: KHAI Tử BindingResult trong controller!
    // BindingResult là anti-pattern với @ControllerAdvice
}
```

### 4.3 Rate Limiting (Chống Brute Force)

> **Dependency** (bucket4j 8.x + Spring Boot 3):
> ```xml
> <dependency>
>   <groupId>com.bucket4j</groupId>
>   <artifactId>bucket4j-core</artifactId>
>   <version>8.10.1</version>
> </dependency>
> ```

```java
// Dùng Bucket4j 8.x — API mới nhất (không dùng Bandwidth.simple() cũ)
@Component
@Slf4j
public class RateLimitFilter extends OncePerRequestFilter {

    // Mỗi IP có 1 bucket riêng, cho phép 5 request/phút trên /api/auth/login
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.builder()
            .capacity(5)
            .refillGreedy(5, Duration.ofMinutes(1))  // Refill 5 token mỗi phút
            .build();
        return Bucket.builder().addLimit(limit).build();
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        if ("/api/auth/login".equals(request.getRequestURI())) {
            String ip = request.getRemoteAddr();
            Bucket bucket = buckets.computeIfAbsent(ip, k -> newBucket());

            if (!bucket.tryConsume(1)) {
                log.warn("Rate limit exceeded for IP: {}", ip);
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                var problem = ProblemDetail.forStatusAndDetail(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Quá nhiều request. Thử lại sau 1 phút.");
                new ObjectMapper().writeValue(response.getWriter(), problem);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        // Chỉ áp dụng cho login endpoint
        return !"/api/auth/login".equals(request.getRequestURI());
    }
}
```

### 4.4 Lưu Secret Keys An Toàn — 12-Factor App

```yaml
# application.yml — KHAI Tử hardcode secrets
# ❌ SAI
app:
  jwt:
    secret: "myHardcodedSecret"

# ✅ ĐÚNG — tham chiếu tới env var, có default khi test
app:
  jwt:
    secret: ${JWT_SECRET}             # bắt buộc có (nhờ validate!)
    access-expiration-ms: ${JWT_ACCESS_EXPIRATION_MS:900000}
    refresh-expiration-ms: ${JWT_REFRESH_EXPIRATION_MS:604800000}
```

```bash
# .env (local dev) — KHAI Tử commit file này lên git!
JWT_SECRET=your-generated-512-bit-base64-key-here
GOOGLE_CLIENT_ID=...
GOOGLE_CLIENT_SECRET=...
```

```java
// ✅ @ConfigurationProperties + @Validated tự thông báo khi thiếu config
@ConfigurationProperties(prefix = "app.jwt")
@Validated
public record JwtProperties(
    @NotBlank String secret,
    @Positive long accessExpirationMs,
    @Positive long refreshExpirationMs
) {}
// Khi khởi động thiếu JWT_SECRET → fail fast với thông báo rõ ràng
```

> **Production**: Dùng **AWS Secrets Manager**, **Azure Key Vault**, hoặc **HashiCorp Vault** thay vì environment variable.

### 4.5 HTTPS Trong Production

```yaml
# application-prod.yml
server:
  ssl:
    enabled: true
    key-store: classpath:keystore.p12
    key-store-password: ${SSL_KEY_STORE_PASSWORD}
    key-store-type: PKCS12
    key-alias: tomcat
  port: 443
```

```java
// Redirect HTTP sang HTTPS
@Bean
public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
    http
        // Bắt buộc HTTPS trong production
        .requiresChannel(channel -> channel
            .anyRequest().requiresSecure()
        )
        // HSTS: Bảo trình duyệt luôn dùng HTTPS
        .headers(headers -> headers
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000) // 1 năm
            )
        );
    return http.build();
}
```

### 4.6 Security Headers — Spring Security 6.4.x

```java
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .headers(headers -> headers
            // Chống Clickjacking (SAMEORIGIN hoặc DENY)
            .frameOptions(frame -> frame.deny())

            // Chống MIME sniffing (buộc browser theo đúng Content-Type)
            .contentTypeOptions(Customizer.withDefaults())

            // HSTS — chỉ bật khi đã có HTTPS
            .httpStrictTransportSecurity(hsts -> hsts
                .includeSubDomains(true)
                .maxAgeInSeconds(31536000) // 1 năm
                .preload(true)
            )

            // Content Security Policy
            .contentSecurityPolicy(csp -> csp
                .policyDirectives(
                    "default-src 'self'; " +
                    "script-src 'self'; " +
                    "img-src 'self' data: https:; " +
                    "style-src 'self' 'unsafe-inline'; " +
                    "frame-ancestors 'none'"
                ))

            // Referrer Policy
            .referrerPolicy(referrer -> referrer
                .policy(ReferrerPolicyHeaderWriter.ReferrerPolicy
                    .STRICT_ORIGIN_WHEN_CROSS_ORIGIN))

            // Permissions Policy (thay cho Feature-Policy cũ)
            .permissionsPolicy(permissions -> permissions
                .policy("camera=(), microphone=(), geolocation=()"))
        )
        .build();
}
```

### 4.7 Logging & Monitoring

```java
@Component
@Slf4j
public class SecurityAuditLog {

    @EventListener
    public void onAuthenticationSuccess(
            AuthenticationSuccessEvent event) {
        log.info("LOGIN SUCCESS - User: {}, IP: {}",
            event.getAuthentication().getName(),
            getClientIP());
    }

    @EventListener
    public void onAuthenticationFailure(
            AbstractAuthenticationFailureEvent event) {
        log.warn("LOGIN FAILED - User: {}, Reason: {}, IP: {}",
            event.getAuthentication().getName(),
            event.getException().getMessage(),
            getClientIP());
    }

    /**
     * Lấy IP thực của client — xử lý trường hợp đứng sau reverse proxy/load balancer.
     * X-Forwarded-For header chứa: client-ip, proxy1-ip, proxy2-ip, ...
     */
    private String getClientIP() {
        HttpServletRequest request = ((ServletRequestAttributes)
            RequestContextHolder.currentRequestAttributes()).getRequest();
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null || xfHeader.isBlank()) {
            return request.getRemoteAddr();
        }
        // Lấy IP đầu tiên (IP gốc của client)
        return xfHeader.split(",")[0].trim();
    }
}
```

---

## 5. Security Checklist

### Xác thực & Phân quyền
- [ ] Mã hóa password với BCrypt (cost factor >= 10)
- [ ] Không lưu plain text password
- [ ] Validate JWT signature và expiration
- [ ] Áp dụng principle of least privilege (chỉ cấp quyền tối thiểu cần thiết)
- [ ] Implement role-based access control (RBAC)

### Token
- [ ] Access token có thời gian ngắn (15-60 phút)
- [ ] Refresh token có thời gian dài hơn (7-30 ngày)
- [ ] Lưu JWT secret trong environment variable, không hardcode
- [ ] JWT secret đủ dài (>= 256 bits)
- [ ] Implement token revocation khi cần (logout, đổi password)

### CORS & CSRF
- [ ] Cấu hình CORS chỉ cho phép domain cụ thể
- [ ] Không dùng `allowedOrigins("*")` trong production
- [ ] Tắt CSRF cho REST API dùng JWT
- [ ] Bật CSRF cho web app dùng cookie/session

### Input & Output
- [ ] Validate tất cả input từ client
- [ ] Không trả về thông tin nhạy cảm trong response
- [ ] Sanitize data để chống XSS
- [ ] Dùng parameterized queries để chống SQL Injection

### Infrastructure
- [ ] Dùng HTTPS trong production
- [ ] Cấu hình security headers (HSTS, CSP, X-Frame-Options)
- [ ] Rate limiting cho login endpoint
- [ ] Log và monitor authentication events
- [ ] Lưu secrets trong environment variables hoặc vault

---

## 6. SecurityConfig Hoàn Chỉnh — Spring Security 6.4.x

```java
// config/SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // @PreAuthorize, @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter       jwtAuthFilter;
    private final JwtAuthEntryPoint             jwtAuthEntryPoint;
    private final CorsConfigurationSource       corsConfigurationSource;
    private final UserDetailsServiceImpl        userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        return http
            // ── CORS ──
            .cors(cors -> cors.configurationSource(corsConfigurationSource))

            // ── CSRF: tắt cho JWT REST API stateless ──
            .csrf(csrf -> csrf.disable())

            // ── Security Headers ──
            .headers(h -> h
                .frameOptions(f -> f.deny())
                .contentTypeOptions(Customizer.withDefaults())
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true).maxAgeInSeconds(31536000))
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; frame-ancestors 'none'"))
            )

            // ── Session: STATELESS ──
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── Xử lý lỗi 401 / 403 ──
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthEntryPoint)
                .accessDeniedHandler((req, res, e) -> {
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    // ProblemDetail JSON ─ RFC 9457
                    var problem = ProblemDetail.forStatusAndDetail(
                        HttpStatus.FORBIDDEN,
                        "Bạn không có quyền thực hiện hành động này");
                    new ObjectMapper().writeValue(res.getWriter(), problem);
                })
            )

            // ── Phân quyền URL ──
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            // ── JWT Filter ──
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class)

            .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);  // 12 rounds chuẩn 2025+
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }
}
```

---

## 7. Tóm Tắt

```
BCrypt
  └── Luôn encode password trước khi lưu DB
  └── Không so sánh plain text, dùng matches()

CORS
  └── Cho phép domain cụ thể (không dùng * trong production)
  └── Cấu hình allowedMethods, allowedHeaders, allowCredentials

CSRF
  └── Tắt cho REST API dùng JWT (stateless)
  └── Bật cho web app dùng cookie/session

Other
  └── Validate input (@Valid, @NotBlank, @Size)
  └── Dùng DTO, không expose entity trực tiếp
  └── Lưu secrets trong env vars
  └── HTTPS trong production
  └── Rate limiting cho login
  └── Log security events
```

---

## 8. Bài Tập Thực Hành

1. Kiểm tra lại project: tất cả password đều được BCrypt encode?
2. Cấu hình CORS cho frontend `localhost:3000`
3. Thêm validation cho `RegisterRequest` và `LoginRequest`
4. Tạo DTO cho response (không trả về password)
5. Đọc secrets từ environment variables thay vì hardcode
6. Thêm security headers vào `SecurityConfig`
7. Implement `AccessDeniedHandler` custom trả về JSON thay vì HTML

---

## Tổng Kết Bài 12

| Bài | Nội dung | Trạng thái |
|-----|----------|------------|
| 12.1 | Authentication & Authorization | [01_Authentication_Authorization.md](01_Authentication_Authorization.md) |
| 12.2 | JWT | [02_JWT.md](02_JWT.md) |
| 12.3 | OAuth2 | [03_OAuth2.md](03_OAuth2.md) |
| 12.4 | Best Practices | 📌 Bạn đang ở đây |

> Phase tiếp theo: **Phase 5 - Nâng Cao** (Validation, Design Patterns, Testing, Swagger, Redis)
