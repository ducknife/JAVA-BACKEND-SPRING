# Bài 12.2: JWT (JSON Web Token)

> **Stack**: Spring Boot 3.4.3 · JJWT **0.12.6** · Java 17+

## 1. JWT Là Gì?

**JWT (JSON Web Token)** là chuẩn mở RFC 7519 định nghĩa cách truyền thông tin an toàn giữa các bên dưới dạng JSON object được **ký số**.

### Tại sao dùng JWT thay vì Session?

| | Session-based | JWT (Token-based) |
|---|---|---|
| Lưu trữ | Server lưu session | Client lưu token |
| Scalability | Khó scale (cần shared session) | Dễ scale (stateless) |
| Phù hợp | Web truyền thống | REST API, Microservices |
| Revoke token | Dễ (xóa session) | Cần blacklist / short TTL |
| CORS | Phức tạp với cookie | Đơn giản (Authorization header) |

---

## 2. Cấu Trúc JWT

JWT gồm 3 phần, ngăn cách nhau bởi dấu `.`:

```
xxxxx.yyyyy.zzzzz
  │      │      │
Header Payload Signature
```

### 2.1 Header

```json
{
  "alg": "HS256",   // Thuật toán ký: HS256, RS256, ...
  "typ": "JWT"
}
```

### 2.2 Payload (Claims)

```json
{
  "sub": "1234567890",    // Subject (thường là userId/username)
  "name": "nguyen_van_a",
  "roles": ["ROLE_USER"],
  "iat": 1709000000,       // Issued At (thời điểm tạo)
  "exp": 1709086400        // Expiration Time (thời điểm hết hạn)
}
```

> ⚠️ **Lưu ý**: Payload **không được mã hóa**, chỉ được **Base64 encode**. Đừng lưu thông tin nhạy cảm (password, credit card) vào đây.

### 2.3 Signature

```
HMACSHA256(
  base64UrlEncode(header) + "." + base64UrlEncode(payload),
  secretKey
)
```

Signature đảm bảo token **không bị giả mạo** hoặc **bị sửa đổi**.

---

## 3. Luồng JWT Authentication

```
Client                                    Server
  │                                          │
  │ POST /api/auth/login                     │
  │ { username, password }  ────────────► │
  │                                          │ 1. Xác thực username/password
  │                                          │ 2. Tạo JWT token
  │ ◄──────────────────────── { token }      │
  │                                          │
  │ GET /api/products                        │
  │ Authorization: Bearer <token> ────────► │
  │                                          │ 3. Validate token
  │                                          │ 4. Extract claims
  │                                          │ 5. Set SecurityContext
  │ ◄──────────────────────── [products]     │
```

---

## 4. Triển Khai JWT Trong Spring Boot

### 4.1 Thêm Dependencies

```xml
<!-- pom.xml — JJWT 0.12.6 (mới nhất, 03/2026) -->
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-api</artifactId>
    <version>0.12.6</version>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-impl</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>io.jsonwebtoken</groupId>
    <artifactId>jjwt-jackson</artifactId>
    <version>0.12.6</version>
    <scope>runtime</scope>
</dependency>
```

### 4.2 Cấu Hình — @ConfigurationProperties (chuẩn hơn @Value)

> **Senior note**: Dùng `@ConfigurationProperties` thay vì `@Value` cho nhiều property cùng prefix — type-safe, dễ validate, dễ test.

```yaml
# application.yml
app:
  jwt:
    # Sinh secret key chuẩn: openssl rand -base64 64
    # Phải >= 512 bits (64 bytes) khi dùng HS512
    secret: ${JWT_SECRET}
    access-expiration-ms: 900000       # 15 phút — KHUYÊN DÙNG (không phải 24h!)
    refresh-expiration-ms: 604800000   # 7 ngày
```

```java
// config/JwtProperties.java
@ConfigurationProperties(prefix = "app.jwt")
@Validated                          // Validate khi app khởi động
public record JwtProperties(
    @NotBlank String secret,
    @Positive long accessExpirationMs,
    @Positive long refreshExpirationMs
) {}
```

```java
// Application.java — bật @ConfigurationProperties
@SpringBootApplication
@ConfigurationPropertiesScan        // quét tất cả @ConfigurationProperties trong package
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

> ⚠️ **Lưu ý bảo mật**: Access token nên **15-60 phút**, KHÔNG phải 24 giờ. Token ngắn = ít rủi ro nếu bị lộ. Dùng Refresh Token để lấy Access Token mới.

### 4.3 JwtService — Tạo / Parse / Validate Token

```java
// security/jwt/JwtService.java
@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final JwtProperties jwtProperties;

    // ── Key ────────────────────────────────────────────────────
    /**
     * Dùng HS512 (512-bit) thay vì HS256 (256-bit) — mạnh hơn, key >= 64 bytes.
     * Tạo secret: openssl rand -base64 64
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.secret());
        return Keys.hmacShaKeyFor(keyBytes);  // JJWT tự chọn HS512 nếu key đủ dài
    }

    // ── Generate ───────────────────────────────────────────────
    public String generateAccessToken(UserPrincipal principal) {
        return buildToken(Map.of(
            "roles", principal.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList(),
            "userId", principal.getId()
        ), principal.getUsername(), jwtProperties.accessExpirationMs());
    }

    public String generateAccessToken(UserDetails userDetails) {
        return buildToken(Map.of(), userDetails.getUsername(),
            jwtProperties.accessExpirationMs());
    }

    private String buildToken(Map<String, Object> extraClaims,
                               String subject, long expirationMs) {
        var now = Instant.now();
        return Jwts.builder()
            .claims(extraClaims)
            .subject(subject)
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusMillis(expirationMs)))
            .signWith(getSigningKey())     // JJWT 0.12 tự suy luận thuật toán từ key
            .compact();
    }

    // ── Extract ───────────────────────────────────────────────
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Instant extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration).toInstant();
    }

    public <T> T extractClaim(String token,
                               Function<Claims, T> claimsResolver) {
        return claimsResolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)      // ném JwtException nếu không hợp lệ
            .getPayload();
    }

    // ── Validate ──────────────────────────────────────────────
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            return username.equals(userDetails.getUsername())
                && !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Token không hợp lệ: {}", e.getMessage());
            return false;
        }
    }

    /** Chỉ kiểm tra token có parse được không (không cần UserDetails) */
    public boolean validateTokenStructure(String token) {
        try {
            Jwts.parser().verifyWith(getSigningKey()).build()
                .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token hết hạn");
        } catch (MalformedJwtException e) {
            log.debug("Token sai định dạng");
        } catch (UnsupportedJwtException e) {
            log.debug("Token không được hỗ trợ");
        } catch (SecurityException e) {
            log.debug("Chữ ký JWT không hợp lệ");
        } catch (IllegalArgumentException e) {
            log.debug("Token rỗng hoặc null");
        }
        return false;
    }

    private boolean isTokenExpired(String token) {
        return extractExpiration(token).isBefore(Instant.now());
    }

    public long getAccessExpirationMs() {
        return jwtProperties.accessExpirationMs();
    }
}
```

### 4.4 JwtAuthenticationFilter

```java
// security/jwt/JwtAuthenticationFilter.java
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService               jwtService;
    private final UserDetailsServiceImpl   userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest  request,
                                    HttpServletResponse response,
                                    FilterChain         filterChain)
            throws ServletException, IOException {

        // 1. Lấy Bearer token từ header
        final String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String jwt      = authHeader.substring(7);
        final String username;

        try {
            username = jwtService.extractUsername(jwt);
        } catch (JwtException ex) {
            // Token lỗi cú pháp / chữ ký — log debug, không throw
            log.debug("Không thể parse JWT: {}", ex.getMessage());
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Chỉ xử lý khi chưa có Authentication trong context
        if (username != null
                && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtService.isTokenValid(jwt, userDetails)) {
                var authToken = UsernamePasswordAuthenticationToken.authenticated(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
                );
                authToken.setDetails(
                    new WebAuthenticationDetailsSource().buildDetails(request));

                // 3. Set vào SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        filterChain.doFilter(request, response);
    }

    /** Bỏ qua filter với các path public — tùy chọn tối ưu hiệu năng */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getServletPath();
        return path.startsWith("/api/auth/")
            || path.startsWith("/api/public/")
            || path.equals("/actuator/health");
    }
}
```

### 4.6 SecurityConfig — Thêm JWT Filter

```java
// config/SecurityConfig.java  (đã có ở bài 12.1, bổ sung JWT)
@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
    return http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session
            .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .exceptionHandling(ex -> ex
            .authenticationEntryPoint(jwtAuthEntryPoint))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers("/api/auth/**").permitAll()
            .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
            .requestMatchers("/actuator/health").permitAll()
            .anyRequest().authenticated()
        )
        // JWT filter TRƯỚC UsernamePasswordAuthenticationFilter
        .addFilterBefore(jwtAuthFilter,
            UsernamePasswordAuthenticationFilter.class)
        .build();
}
```

### 4.5 JwtAuthEntryPoint — Trả JSON khi 401

```java
// security/JwtAuthEntryPoint.java
@Component
public class JwtAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(HttpServletRequest      request,
                         HttpServletResponse     response,
                         AuthenticationException authException) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        // Dùng ProblemDetail chuẩn RFC 9457 (Spring 6+)
        var problem = ProblemDetail.forStatusAndDetail(
            HttpStatus.UNAUTHORIZED,
            "Bạn cần đăng nhập để truy cập tài nguyên này");
        problem.setTitle("Unauthorized");
        problem.setInstance(URI.create(request.getServletPath()));
        problem.setProperty("timestamp", Instant.now().toString());

        objectMapper.writeValue(response.getOutputStream(), problem);
    }
}
```

### 4.7 AuthController — Trả Về Token

```java
// auth/AuthController.java
@PostMapping("/login")
public ResponseEntity<AuthResponse> login(
        @Valid @RequestBody LoginRequest request) {

    // 1. Spring Security xác thực — ném BadCredentialsException nếu sai
    Authentication authentication = authenticationManager.authenticate(
        new UsernamePasswordAuthenticationToken(
            request.username(), request.password()  // Java record
        )
    );

    // 2. Lấy UserPrincipal từ Authentication
    var principal = (UserPrincipal) authentication.getPrincipal();

    // 3. Tạo JWT và Refresh Token
    String accessToken  = jwtService.generateAccessToken(principal);
    String refreshToken = refreshTokenService.create(principal.getId()).getToken();

    List<String> roles = principal.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority).toList();

    // 4. Trả về AuthResponse (record)
    return ResponseEntity.ok(new AuthResponse(
        accessToken,
        refreshToken,
        jwtService.getAccessExpirationMs(),
        principal.getUsername(),
        roles
    ));
}
```

**Response mẫu:**
```json
{
  "accessToken":  "eyJhbGciOiJIUzUxMiJ9...",
  "refreshToken": "3a7f8b29-...",
  "tokenType":    "Bearer",
  "expiresIn":    900000,
  "username":     "nguyen_van_a",
  "roles":        ["ROLE_USER"]
}
```

---

## 5. Refresh Token

Khi access token hết hạn (sau 15 phút), dùng refresh token lấy access token mới — không cần đăng nhập lại.

```java
// RefreshToken entity
@Entity
@Table(name = "refresh_tokens")
@Getter @Setter
@NoArgsConstructor
public class RefreshToken {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private String id;          // UUID làm token value

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;

    @Column(nullable = false)
    private boolean revoked = false;
}

// RefreshTokenService
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository         userRepository;
    private final JwtProperties          jwtProperties;

    public RefreshToken create(Long userId) {
        // Xóa refresh token cũ trước khi tạo mới (chỉ giữ 1 token/user)
        refreshTokenRepository.deleteByUserId(userId);

        var token = new RefreshToken();
        token.setUser(userRepository.getReferenceById(userId));
        token.setExpiryDate(
            Instant.now().plusMillis(jwtProperties.refreshExpirationMs()));

        return refreshTokenRepository.save(token);
    }

    public RefreshToken verifyAndGet(String tokenId) {
        return refreshTokenRepository.findById(tokenId)
            .filter(rt -> !rt.isRevoked())
            .filter(rt -> rt.getExpiryDate().isAfter(Instant.now()))
            .orElseThrow(() -> new InvalidTokenException(
                "Refresh token không hợp lệ hoặc đã hết hạn"));
    }

    public void revokeByUserId(Long userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }
}

// AuthController — endpoint làm mới token
@PostMapping("/refresh")
public ResponseEntity<AuthResponse> refresh(
        @RequestParam @NotBlank String refreshToken) {

    var rt        = refreshTokenService.verifyAndGet(refreshToken);
    var principal = UserPrincipal.create(rt.getUser());
    var newAccess  = jwtService.generateAccessToken(principal);
    var newRefresh = refreshTokenService.create(rt.getUser().getId()).getId();

    List<String> roles = principal.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority).toList();

    return ResponseEntity.ok(new AuthResponse(
        newAccess, newRefresh,
        jwtService.getAccessExpirationMs(),
        principal.getUsername(), roles
    ));
}
```

---

## 6. Sinh Secret Key Đúng Cách

```bash
# Cách 1: openssl (khuyên dùng, sinh 512-bit key)
openssl rand -base64 64

# Cách 2: Java
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.SignatureAlgorithm;
import java.util.Base64;

SecretKey key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
String base64Key = Base64.getEncoder().encodeToString(key.getEncoded());
System.out.println(base64Key); // Copy vào .env

# Output mẫu:
# rE3x... (88 chars base64 = 64 bytes = 512 bits)
```

```bash
# .env hoặc environment variable (KHÔNG commit lên git!)
JWT_SECRET=rE3xYourGeneratedKeyHere...
```

```gitignore
# .gitignore
.env
*.env
```

---

## 7. Test JWT Với Postman / HTTPie

### Đăng nhập lấy token
```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{
  "username": "admin",
  "password": "Admin@123"
}
```

### Gọi API bảo vệ
```http
GET http://localhost:8080/api/products
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### Làm mới token
```http
POST http://localhost:8080/api/auth/refresh?refreshToken=3a7f8b29-...
```

---

## 7. Tóm Tắt

```
JWT = Header.Payload.Signature
Khuyên dùng: HS512 (key >= 64 bytes / 512 bits)

Luồng:
POST /login → authenticate → tạo accessToken (15 phút) + refreshToken (7 ngày)
GET /resource + Bearer token → JwtFilter validate → SecurityContext → Controller
POST /refresh + refreshToken → tạo accessToken mới

Components (Spring Boot 3.4.x):
  JwtProperties         → @ConfigurationProperties, validate khi startup
  JwtService            → buildToken / extractUsername / isTokenValid
  JwtAuthenticationFilter → extends OncePerRequestFilter, shouldNotFilter()
  JwtAuthEntryPoint     → implements AuthenticationEntryPoint → ProblemDetail JSON
  RefreshTokenService   → UUID token, revoke khi logout / đổi password
```

> ⚠️ **Key Security Rules**:
> 1. Access token: **15-60 phút** (không phải 24h!)
> 2. Secret key **>= 512 bits** khi dùng HS512 — sinh bằng `openssl rand -base64 64`
> 3. Lưu secret trong **environment variable**, không hardcode
> 4. Revoke refresh token khi user **logout hoặc đổi password**

---

## 9. Bài Tập Thực Hành

1. Sinh secret key bằng `openssl rand -base64 64`, lưu vào `.env`
2. Tạo `JwtProperties` record với `@ConfigurationProperties`
3. Implement `JwtService`: `generateAccessToken`, `isTokenValid`, `extractUsername`
4. Tạo `JwtAuthenticationFilter` với `shouldNotFilter()` skip public paths
5. Tạo `JwtAuthEntryPoint` trả `ProblemDetail` JSON
6. Implement `RefreshTokenService` với revoke khi logout
7. Test chuỗi:
   - Login → nhận `accessToken` (15') + `refreshToken`
   - Gọi API với token → 200
   - Đợi token hết hạn (hoặc dùng token giả) → 401 JSON
   - Gọi `/refresh` → nhận `accessToken` mới
   - Logout → gọi lại `/refresh` → lỗi

> Tiếp theo: [03_OAuth2.md](03_OAuth2.md) — Đăng nhập qua Google / GitHub
