# Bài 12.1: Authentication & Authorization trong Spring Security

> **Stack**: Spring Boot 3.4.3 · Spring Security 6.4.x · Java 17+

## 1. Khái Niệm Cơ Bản

### Authentication (Xác thực) là gì?
> "**Bạn là ai?**" — Xác minh danh tính người dùng

- Người dùng cung cấp **credentials** (username + password, token, ...)
- Hệ thống kiểm tra credentials có hợp lệ không
- Nếu hợp lệ → cấp **Authentication object** vào `SecurityContext`

### Authorization (Phân quyền) là gì?
> "**Bạn được làm gì?**" — Kiểm tra quyền truy cập tài nguyên

- Sau khi xác thực, hệ thống kiểm tra user có **quyền** (role/permission) để thực hiện hành động không
- VD: User có role `ADMIN` mới được xóa sản phẩm

---

## 2. Luồng Hoạt Động Của Spring Security

```
Request đến
    │
    ▼
SecurityFilterChain (Chuỗi Filter)
    │
    ├─ JwtAuthenticationFilter         ← custom, chạy trước
    ├─ UsernamePasswordAuthFilter      ← form login
    ├─ BasicAuthenticationFilter       ← Basic Auth
    │
    ▼
AuthenticationManager
    │
    ▼
DaoAuthenticationProvider
    ├─ UserDetailsService.loadUserByUsername()
    └─ PasswordEncoder.matches()
    │
    ▼
Authentication thành công → SecurityContextHolder
    │
    ▼
AuthorizationFilter
    ├─ .hasRole() / .hasAuthority()
    └─ @PreAuthorize SpEL
    │
    ▼
Controller (nếu có quyền)
```

---

## 3. Các Thành Phần Quan Trọng

| Thành phần | Vai trò |
|------------|---------|
| `SecurityFilterChain` | Chuỗi filter xử lý request bảo mật |
| `AuthenticationManager` | Điều phối quá trình xác thực |
| `DaoAuthenticationProvider` | Xác thực bằng UserDetailsService + PasswordEncoder |
| `UserDetailsService` | Load thông tin user từ DB |
| `UserDetails` | Đối tượng chứa thông tin user (implements interface này) |
| `PasswordEncoder` | Mã hóa & kiểm tra password |
| `SecurityContextHolder` | ThreadLocal lưu Authentication của request hiện tại |

---

## 4. Triển Khai Thực Tế

### 4.1 Entity User & Role

```java
// ERole.java
public enum ERole {
    ROLE_USER,
    ROLE_MODERATOR,
    ROLE_ADMIN
}

// Role.java
@Entity
@Table(name = "roles")
@Getter @Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false, length = 20)
    private ERole name;
}

// User.java
@Entity
@Table(name = "users",
    uniqueConstraints = {
        @UniqueConstraint(columnNames = "username"),
        @UniqueConstraint(columnNames = "email")
    }
)
@Getter @Setter
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String username;

    @Column(nullable = false, length = 120)
    private String password;          // luôn BCrypt — KHÔNG lưu plain text

    @Column(nullable = false, length = 100)
    private String email;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns     = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
```

### 4.2 UserPrincipal — implements UserDetails

> **Senior note**: Đừng trả thẳng `User` entity về `UserDetails`. Tạo riêng `UserPrincipal` để tách biệt domain model và security layer.

```java
// security/UserPrincipal.java
@Getter
@EqualsAndHashCode(of = "id")
public class UserPrincipal implements UserDetails {

    private final Long   id;
    private final String email;
    private final String username;

    @JsonIgnore                          // KHÔNG serialize password ra JSON
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    private UserPrincipal(Long id, String email, String username,
                          String password,
                          Collection<? extends GrantedAuthority> authorities) {
        this.id          = id;
        this.email       = email;
        this.username    = username;
        this.password    = password;
        this.authorities = authorities;
    }

    /** Factory method — dùng thay vì constructor trực tiếp */
    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName().name()))
            .toList();                   // Java 16+ (không cần Collectors.toList())

        return new UserPrincipal(
            user.getId(),
            user.getEmail(),
            user.getUsername(),
            user.getPassword(),
            authorities
        );
    }

    // Các method còn lại của UserDetails — mặc định trả true (không bị khóa)
    @Override public boolean isAccountNonExpired()  { return true; }
    @Override public boolean isAccountNonLocked()   { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }
    @Override public boolean isEnabled()            { return true; }
}
```

### 4.3 UserDetailsServiceImpl

```java
// security/UserDetailsServiceImpl.java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Không tìm thấy user với username: " + username));

        return UserPrincipal.create(user);
    }
}
```

### 4.4 SecurityConfig — Chuẩn Spring Security 6.4.x

```java
// config/SecurityConfig.java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity          // Bật @PreAuthorize, @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final JwtAuthEntryPoint       jwtAuthEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        return http
            // ── CORS (phải khai báo, Spring Security 6 không tự configure) ──
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── CSRF: tắt cho REST API stateless ──
            .csrf(csrf -> csrf.disable())

            // ── Session: STATELESS vì dùng JWT ──
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── Xử lý lỗi 401 / 403 ──
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthEntryPoint)
                .accessDeniedHandler((req, res, e) -> {
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    res.getWriter().write("""
                        {"status":403,"error":"Forbidden",
                         "message":"Bạn không có quyền thực hiện hành động này"}
                        """);
                })
            )

            // ── Phân quyền URL ──
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .anyRequest().authenticated()
            )

            // ── JWT filter chạy trước UsernamePasswordAuthenticationFilter ──
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class)

            .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // 12 rounds — chuẩn production 2025+
    }

    /**
     * Spring Security 6: nếu UserDetailsService & PasswordEncoder là Bean,
     * DaoAuthenticationProvider được tự động cấu hình.
     * Khai báo tường minh chỉ khi cần override hành vi mặc định.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        var config = new CorsConfiguration();
        config.setAllowedOrigins(List.of(
            "http://localhost:3000",
            "http://localhost:5173"
        ));
        config.setAllowedMethods(List.of("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization","Content-Type","Accept"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        var source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
```

### 4.5 Phân Quyền Ở Method Level — @PreAuthorize

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    // Chỉ ADMIN và MODERATOR
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping
    public ResponseEntity<ProductResponse> create(
            @Valid @RequestBody ProductRequest req) { ... }

    // Chỉ ADMIN
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }

    // User chỉ được sửa tài nguyên của chính mình
    @PreAuthorize("@productService.isOwner(#id, authentication.name)")
    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductRequest req) { ... }
}
```

---

## 5. DTO Dùng Java Record (Java 16+)

```java
// auth/dto/RegisterRequest.java
public record RegisterRequest(
    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username 3-50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
             message = "Username chỉ chứa chữ, số và gạch dưới")
    String username,

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password tối thiểu 8 ký tự")
    String password,

    @Email(message = "Email không hợp lệ")
    @NotBlank
    String email,

    Set<String> roles                  // tuỳ chọn, null → ROLE_USER
) {}

// auth/dto/LoginRequest.java
public record LoginRequest(
    @NotBlank String username,
    @NotBlank String password
) {}

// auth/dto/AuthResponse.java
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long   expiresIn,
    String username,
    List<String> roles
) {
    public AuthResponse(String accessToken, String refreshToken,
                        long expiresIn, String username, List<String> roles) {
        this(accessToken, refreshToken, "Bearer", expiresIn, username, roles);
    }
}
```

---

## 6. AuthController & AuthService

### AuthController

```java
// auth/AuthController.java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponse register(@Valid @RequestBody RegisterRequest request) {
        return authService.register(request);
    }

    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }

    @PostMapping("/refresh")
    public AuthResponse refresh(@RequestParam String refreshToken) {
        return authService.refreshToken(refreshToken);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(
            @AuthenticationPrincipal UserPrincipal principal) {
        authService.logout(principal.getId());
    }
}
```

### AuthService

```java
// auth/AuthService.java
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final UserRepository         userRepository;
    private final RoleRepository         roleRepository;
    private final PasswordEncoder        passwordEncoder;
    private final AuthenticationManager  authenticationManager;
    private final JwtService             jwtService;
    private final RefreshTokenService    refreshTokenService;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByUsername(req.username())) {
            throw new ConflictException("Username '" + req.username() + "' đã tồn tại");
        }
        if (userRepository.existsByEmail(req.email())) {
            throw new ConflictException("Email '" + req.email() + "' đã được sử dụng");
        }

        var user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password())); // ← luôn encode

        Set<Role> roles = resolveRoles(req.roles());
        user.setRoles(roles);
        userRepository.save(user);

        var principal    = UserPrincipal.create(user);
        var accessToken  = jwtService.generateAccessToken(principal);
        var refreshToken = refreshTokenService.create(user.getId());

        return buildResponse(accessToken, refreshToken.getToken(), principal);
    }

    public AuthResponse login(LoginRequest req) {
        // Authenticate — ném BadCredentialsException nếu sai
        var auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(req.username(), req.password())
        );
        var principal    = (UserPrincipal) auth.getPrincipal();
        var accessToken  = jwtService.generateAccessToken(principal);
        var refreshToken = refreshTokenService.create(
            userRepository.findByUsername(req.username()).orElseThrow().getId()
        );

        return buildResponse(accessToken, refreshToken.getToken(), principal);
    }

    private AuthResponse buildResponse(String accessToken, String refreshToken,
                                        UserPrincipal principal) {
        var roles = principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .toList();

        return new AuthResponse(
            accessToken,
            refreshToken,
            jwtService.getAccessExpirationMs(),
            principal.getUsername(),
            roles
        );
    }

    private Set<Role> resolveRoles(Set<String> strRoles) {
        if (strRoles == null || strRoles.isEmpty()) {
            return Set.of(findRole(ERole.ROLE_USER));
        }
        return strRoles.stream()
            .map(r -> switch (r.toLowerCase()) {
                case "admin" -> findRole(ERole.ROLE_ADMIN);
                case "mod"   -> findRole(ERole.ROLE_MODERATOR);
                default      -> findRole(ERole.ROLE_USER);
            })
            .collect(Collectors.toSet());
    }

    private Role findRole(ERole name) {
        return roleRepository.findByName(name)
            .orElseThrow(() -> new IllegalStateException(
                "Role " + name + " chưa được seed vào DB"));
    }
}
```

---

## 7. GlobalExceptionHandler — ProblemDetail (RFC 9457)

> **Senior note**: Spring 6 giới thiệu `ProblemDetail` — chuẩn RFC 9457 (RFC 7807 cũ). Dùng thay vì tự tạo error DTO.

```java
// exception/GlobalExceptionHandler.java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /** Validation lỗi — @Valid thất bại */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex,
                                          HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.BAD_REQUEST, "Dữ liệu đầu vào không hợp lệ");
        problem.setTitle("Validation Failed");
        problem.setInstance(URI.create(request.getRequestURI()));

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(e -> errors.put(e.getField(), e.getDefaultMessage()));
        problem.setProperty("errors", errors);
        return problem;
    }

    /** Sai credentials khi login */
    @ExceptionHandler(BadCredentialsException.class)
    public ProblemDetail handleBadCredentials(HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED, "Sai username hoặc password");
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    /** Trùng username / email */
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflict(ConflictException ex,
                                         HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.CONFLICT, ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    /** Resource không tìm thấy */
    @ExceptionHandler(EntityNotFoundException.class)
    public ProblemDetail handleNotFound(EntityNotFoundException ex,
                                        HttpServletRequest request) {
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.NOT_FOUND, ex.getMessage());
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    /** Catch-all */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneral(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {}: {}", request.getRequestURI(), ex.getMessage(), ex);
        return ProblemDetail.forStatusAndDetail(
            HttpStatus.INTERNAL_SERVER_ERROR, "Lỗi hệ thống, vui lòng thử lại sau");
    }
}
```

**Response mẫu khi validation thất bại:**
```json
{
  "type": "about:blank",
  "title": "Validation Failed",
  "status": 400,
  "detail": "Dữ liệu đầu vào không hợp lệ",
  "instance": "/api/auth/register",
  "errors": {
    "username": "Username 3-50 ký tự",
    "email": "Email không hợp lệ"
  }
}
```

---

## 8. Lấy User Đang Đăng Nhập

```java
// Cách 1: @AuthenticationPrincipal — KHUYÊN DÙNG (type-safe)
@GetMapping("/profile")
public ResponseEntity<UserProfileResponse> getProfile(
        @AuthenticationPrincipal UserPrincipal principal) {
    return ResponseEntity.ok(userService.getProfile(principal.getId()));
}

// Cách 2: SecurityContextHolder — dùng trong service không có request context
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
UserPrincipal principal = (UserPrincipal) auth.getPrincipal();

// Cách 3: Helper utility
@Component
public class SecurityUtils {
    public static Optional<UserPrincipal> getCurrentUser() {
        return Optional.ofNullable(SecurityContextHolder.getContext().getAuthentication())
            .filter(Authentication::isAuthenticated)
            .map(Authentication::getPrincipal)
            .filter(p -> p instanceof UserPrincipal)
            .map(UserPrincipal.class::cast);
    }
}
```

---

## 9. Tóm Tắt

```
Authentication (Xác thực)              Authorization (Phân quyền)
─────────────────────────              ──────────────────────────
"Bạn là ai?"                           "Bạn được làm gì?"

Triển khai:                            Triển khai:
  UserPrincipal (implements UserDetails)  .authorizeHttpRequests()
  UserDetailsServiceImpl                  @PreAuthorize("hasRole(...)")
  PasswordEncoder (BCrypt 12)             @PreAuthorize("hasAuthority(...)")
  DaoAuthenticationProvider              @PreAuthorize SpEL expressions
```

---

## 10. Bài Tập Thực Hành

1. Tạo project Spring Boot 3.4.x với Spring Security
2. Tạo DB schema: `users`, `roles`, `user_roles`
3. Implement `UserPrincipal` implements `UserDetails`
4. Implement `UserDetailsServiceImpl` load từ DB
5. Cấu hình `SecurityConfig` với `SecurityFilterChain`
6. Implement `GlobalExceptionHandler` trả `ProblemDetail`
7. Test với Postman:
   - `POST /api/auth/register` → 201 Created
   - `POST /api/auth/login` → 200 + tokens
   - `GET /api/admin/...` không có token → 401 JSON
   - `GET /api/admin/...` với ROLE_USER → 403 JSON

> Tiếp theo: [02_JWT.md](02_JWT.md) — JWT Token chi tiết

---

## 2. Luồng Hoạt Động Của Spring Security

```
Request đến
    │
    ▼
SecurityFilterChain (Chuỗi Filter)
    │
    ├─ UsernamePasswordAuthenticationFilter  ← xử lý form login
    ├─ JwtAuthenticationFilter              ← xử lý JWT (custom)
    ├─ BasicAuthenticationFilter            ← xử lý Basic Auth
    │
    ▼
AuthenticationManager
    │
    ▼
AuthenticationProvider (DaoAuthenticationProvider)
    │
    ├─ UserDetailsService.loadUserByUsername()
    ├─ PasswordEncoder.matches()
    │
    ▼
Authentication (thành công) → lưu vào SecurityContextHolder
    │
    ▼
AuthorizationFilter
    │
    ├─ Kiểm tra Role/Authority
    │
    ▼
Controller (nếu có quyền)
```

---

## 3. Các Thành Phần Quan Trọng

| Thành phần | Vai trò |
|------------|---------|
| `SecurityFilterChain` | Chuỗi filter xử lý request bảo mật |
| `AuthenticationManager` | Điều phối quá trình xác thực |
| `AuthenticationProvider` | Thực hiện xác thực thực tế |
| `UserDetailsService` | Load thông tin user từ DB |
| `UserDetails` | Đối tượng chứa thông tin user |
| `PasswordEncoder` | Mã hóa & kiểm tra password |
| `SecurityContextHolder` | Lưu Authentication của request hiện tại |

---

## 4. Triển Khai Thực Tế

### 4.1 Tạo Entity User & Role

```java
// Role.java
@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private ERole name; // ROLE_USER, ROLE_ADMIN, ROLE_MODERATOR
}

// ERole.java (enum)
public enum ERole {
    ROLE_USER,
    ROLE_MODERATOR,
    ROLE_ADMIN
}

// User.java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
```

### 4.2 Implement UserDetailsService

```java
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "Không tìm thấy user: " + username));

        // Chuyển Set<Role> thành Collection<GrantedAuthority>
        List<GrantedAuthority> authorities = user.getRoles().stream()
            .map(role -> new SimpleGrantedAuthority(role.getName().name()))
            .collect(Collectors.toList());

        return new org.springframework.security.core.userdetails.User(
            user.getUsername(),
            user.getPassword(),
            authorities
        );
    }
}
```

### 4.3 Cấu Hình SecurityFilterChain

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity  // Bật @PreAuthorize, @PostAuthorize
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Tắt CSRF cho REST API (stateless)
            .csrf(csrf -> csrf.disable())

            // Cấu hình session stateless (dùng JWT)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Cấu hình quyền truy cập URL
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()      // public
                .requestMatchers("/api/admin/**").hasRole("ADMIN") // chỉ ADMIN
                .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                .anyRequest().authenticated()                      // còn lại cần login
            );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }
}
```

### 4.4 Phân Quyền Ở Method Level

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    // Chỉ ADMIN và MODERATOR mới tạo được
    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping
    public ResponseEntity<Product> create(@RequestBody ProductRequest req) { ... }

    // Chỉ ADMIN mới xóa được
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) { ... }

    // Kiểm tra sau khi thực thi (ít dùng hơn)
    @PostAuthorize("returnObject.body.username == authentication.name")
    @GetMapping("/me")
    public ResponseEntity<User> getMe() { ... }
}
```

---

## 5. Đăng Ký & Đăng Nhập (Form Login cơ bản)

### Bước 1: Tạo DTO

```java
// RegisterRequest.java
@Data
public class RegisterRequest {
    @NotBlank
    private String username;

    @NotBlank
    @Size(min = 6, max = 40)
    private String password;

    private Set<String> roles; // "admin", "mod", "user"
}

// LoginRequest.java
@Data
public class LoginRequest {
    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
```

### Bước 2: AuthController

```java
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/register")
    public ResponseEntity<String> register(
            @Valid @RequestBody RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.badRequest()
                .body("Username đã được sử dụng!");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        // Gán role
        Set<String> strRoles = request.getRoles();
        Set<Role> roles = new HashSet<>();

        if (strRoles == null) {
            roles.add(roleRepository.findByName(ERole.ROLE_USER).orElseThrow());
        } else {
            strRoles.forEach(role -> {
                switch (role) {
                    case "admin" -> roles.add(
                        roleRepository.findByName(ERole.ROLE_ADMIN).orElseThrow());
                    case "mod" -> roles.add(
                        roleRepository.findByName(ERole.ROLE_MODERATOR).orElseThrow());
                    default -> roles.add(
                        roleRepository.findByName(ERole.ROLE_USER).orElseThrow());
                }
            });
        }

        user.setRoles(roles);
        userRepository.save(user);

        return ResponseEntity.ok("Đăng ký thành công!");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        // Spring Security tự xác thực
        Authentication authentication = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // TODO: Tạo JWT token (xem bài 02_JWT.md)
        return ResponseEntity.ok("Login thành công!");
    }
}
```

---

## 6. SecurityContext & Lấy User Hiện Tại

```java
// Cách 1: Từ SecurityContextHolder
Authentication auth = SecurityContextHolder.getContext().getAuthentication();
String username = auth.getName();
Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();

// Cách 2: Inject qua @AuthenticationPrincipal
@GetMapping("/profile")
public ResponseEntity<User> getProfile(
        @AuthenticationPrincipal UserDetails userDetails) {
    String username = userDetails.getUsername();
    // ...
}

// Cách 3: Dùng Principal (interface của Java)
@GetMapping("/profile")
public ResponseEntity<String> getProfile(Principal principal) {
    return ResponseEntity.ok("Hello " + principal.getName());
}
```

---

## 7. Tóm Tắt

```
Authentication (Xác thực)           Authorization (Phân quyền)
─────────────────────────           ──────────────────────────
"Bạn là ai?"                        "Bạn được làm gì?"

Lớp bảo vệ: Filter Chain           Lớp bảo vệ: AuthorizationFilter
                                                 @PreAuthorize
                                                 .hasRole()

Triển khai:                         Triển khai:
- UserDetailsService                - .authorizeHttpRequests()
- PasswordEncoder                   - @PreAuthorize / @PostAuthorize
- AuthenticationProvider            - hasRole() / hasAuthority()
```

---

## 8. Bài Tập Thực Hành

1. Tạo project Spring Boot với Spring Security
2. Tạo bảng `users`, `roles`, `user_roles` trong MySQL
3. Implement `UserDetailsService` load user từ DB
4. Cấu hình `SecurityFilterChain` với các rule phân quyền:
   - `GET /api/products` → public
   - `POST /api/products` → ADMIN
   - `DELETE /api/products/{id}` → ADMIN
   - `GET /api/users/me` → authenticated
5. Test với Postman

> Tiếp theo: [02_JWT.md](02_JWT.md) — Tích hợp JWT Token
