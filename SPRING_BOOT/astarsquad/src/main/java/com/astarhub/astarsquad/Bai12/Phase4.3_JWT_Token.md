# 🎫 JWT (JSON Web Token) — Triển Khai Theo Spring Security Docs

> **Tích hợp từ**: File 1 (SecurityConfig base) + File 2 (UserDetails, Entities, Exception Handlers)
> **File này tạo**: RsaKeyProperties, JwtConfig, JwtService, AuthController
> **Dùng**: `spring-boot-starter-oauth2-resource-server` (built-in Nimbus JOSE+JWT)

---

## 📑 Mục Lục

- [1. JWT Là Gì?](#1-jwt-là-gì)
  - [1.1 Cấu Trúc JWT — 3 Phần](#11-cấu-trúc-jwt--3-phần)
  - [1.2 Tại Sao JWT Cho REST API?](#12-tại-sao-jwt-cho-rest-api)
- [2. RSA vs HMAC — Chọn Thuật Toán](#2-rsa-vs-hmac--chọn-thuật-toán)
- [3. JWK (JSON Web Key) — Quản Lý Key](#3-jwk-json-web-key--quản-lý-key)
  - [3.1 JWK Là Gì?](#31-jwk-là-gì)
  - [3.2 JwtEncoder vs JwtDecoder](#32-jwtencoder-vs-jwtdecoder)
  - [3.3 JwtAuthenticationConverter](#33-jwtauthenticationconverter)
- [4. Dependency](#4-dependency)
- [5. Tạo RSA Key Pair](#5-tạo-rsa-key-pair)
- [6. Code Triển Khai — Tích Hợp File 1 + File 2](#6-code-triển-khai--tích-hợp-file-1--file-2)
  - [6.1 RsaKeyProperties.java](#61-rsakeypropertiesjava--load-key)
  - [6.2 JwtConfig.java — Encoder, Decoder, Converter](#62-jwtconfigjava--encoder-decoder-converter)
  - [6.3 JwtService.java — Business Logic](#63-jwtservicejava--business-logic-tạo-token)
  - [6.4 SecurityConfig.java — Cập Nhật Hoàn Chỉnh](#64-securityconfigjava--cập-nhật-hoàn-chỉnh)
  - [6.5 AuthController.java](#65-authcontrollerjava--login-register-refresh)
  - [6.6 DTOs](#66-dtos)
- [7. Truy Cập JWT Claims — Trong Controller](#7-truy-cập-jwt-claims--trong-controller)
- [8. Custom Validator & Blacklist](#8-custom-validator--blacklist)
- [9. Luồng Hoàn Chỉnh](#9-luồng-hoàn-chỉnh)
- [✅ Checklist](#-checklist)

---

## 1. JWT Là Gì?

**JWT (JSON Web Token)** là chuẩn mở (RFC 7519) để truyền thông tin an toàn giữa các bên
dưới dạng JSON object đã được **ký số** (digitally signed).

### 1.1 Cấu Trúc JWT — 3 Phần

```
eyJhbGciOiJSUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInNjb3BlIjoiUk9MRV9BRE1JTiJ9.signature
|______ Header ______|.________________ Payload __________________|.___ Signature ___|
```

**Header** — Metadata về token:
```json
{
  "alg": "RS256",    // Thuật toán ký: RSA + SHA-256
  "typ": "JWT"       // Loại token
}
```

**Payload (Claims)** — Dữ liệu thực tế:
```json
{
  "iss": "self",                    // Issuer: ai phát hành token
  "sub": "admin",                  // Subject: token đại diện cho ai
  "iat": 1714296000,               // Issued At: tạo lúc nào (Unix timestamp)
  "exp": 1714299600,               // Expiration: hết hạn lúc nào
  "scope": "ROLE_ADMIN user:read", // Custom claim: quyền
  "userId": 42                     // Custom claim: thông tin thêm
}
```

> **Claims** = các cặp key-value trong payload.
> Có 3 loại: **Registered** (iss, sub, exp — chuẩn RFC), **Public** (được đăng ký IANA),
> **Private** (custom — scope, userId, email...).

**Signature** — Chữ ký số:
```
SIGNATURE = RS256(
    base64(header) + "." + base64(payload),
    privateKey
)
```
> Server ký bằng **private key**, ai cũng verify được bằng **public key**.
> Nếu payload bị sửa → signature không khớp → token bị reject.

### 1.2 Tại Sao JWT Cho REST API?

```
Session-based:                          JWT-based:
Client → Server (lưu session trên RAM) Client → Server (STATELESS)
         ↑ scale khó (sticky session)            ↑ scale dễ (bất kỳ server nào verify được)
         ↑ mỗi request lookup session            ↑ self-contained (chứa sẵn user info)
```

---

## 2. RSA vs HMAC — Chọn Thuật Toán

### 2.1 Khái Niệm

**Đối xứng (HMAC/HS256)**: 1 secret key chung — vừa ký vừa verify.
**Bất đối xứng (RSA/RS256)**: 2 key — private key ký, public key verify.

```
Đối xứng (HMAC)                    Bất đối xứng (RSA)
┌──────────┐                       ┌──────────┐   ┌──────────┐
│ Secret   │ ← ký + verify        │ Private  │   │ Public   │
│ Key      │    cùng 1 key         │ Key (ký) │   │ Key      │
└──────────┘                       └──────────┘   │ (verify) │
                                                  └──────────┘
```

| | HMAC (HS256) | RSA (RS256) ✅ |
|---|---|---|
| **Key** | 1 secret key | Private + Public key |
| **Bảo mật** | Lộ key = giả mạo được | Private key giữ kín |
| **Microservice** | Mọi service cần secret | Service chỉ cần public key |
| **Spring docs** | Hỗ trợ | **Recommended** |
| **Dùng khi** | Monolith, dev | Production, microservice |

---

## 3. JWK (JSON Web Key) — Quản Lý Key

### 3.1 JWK Là Gì?

**JWK** = chuẩn biểu diễn cryptographic key dưới dạng JSON.
Spring Security dùng JWK để quản lý key cho JWT.

```
JWK (JSON Web Key)
│
├── RSAKey (bất đối xứng)
│     ├── RSAPublicKey   → verify signature
│     └── RSAPrivateKey  → tạo signature
│
├── OctetSequenceKey (đối xứng)
│     └── SecretKey      → vừa ký vừa verify
│
├── JWKSet
│     └── Tập hợp nhiều JWK (hỗ trợ key rotation)
│
└── JWKSource<SecurityContext>
      └── ImmutableJWKSet → cung cấp JWKSet cho JwtEncoder
```

### 3.2 JwtEncoder vs JwtDecoder

```
JwtEncoder                              JwtDecoder
│                                       │
│ Input: JwtClaimsSet (payload)         │ Input: String token ("eyJ...")
│ Process: Claims → JSON → Base64      │ Process: Base64 decode
│          → Ký bằng Private Key        │          → Verify bằng Public Key
│ Output: Jwt → .getTokenValue()        │          → Check expiry
│         = "eyJ..."                    │ Output: Jwt object (claims, headers)
│                                       │ Throws: JwtException nếu invalid
│ Implementation:                       │ Implementation:
│   NimbusJwtEncoder                    │   NimbusJwtDecoder
│   (cần JWKSource chứa private key)    │   (cần public key hoặc secret key)
```

### 3.3 JwtAuthenticationConverter

```
Khi BearerTokenAuthenticationFilter nhận JWT hợp lệ:

Jwt token (đã decode)
    │
    ▼
JwtAuthenticationConverter
    │
    ├── 1. Đọc claim "scope": "ROLE_ADMIN ROLE_USER user:read"
    ├── 2. Split: ["ROLE_ADMIN", "ROLE_USER", "user:read"]
    ├── 3. Map → SimpleGrantedAuthority cho mỗi item
    └── 4. Tạo JwtAuthenticationToken(jwt, authorities)
                │
                ▼
         SecurityContext (user authenticated!)
```

---

## 4. Dependency

```xml
<!-- CHỈ CẦN 1 dependency — đã bao gồm Nimbus JOSE+JWT -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
</dependency>
```

---

## 5. Tạo RSA Key Pair

```bash
# Tạo private key (PKCS#8 format)
openssl genpkey -algorithm RSA -out src/main/resources/keys/private.pem \
    -pkeyopt rsa_keygen_bits:2048

# Tạo public key từ private key
openssl rsa -pubout -in src/main/resources/keys/private.pem \
    -out src/main/resources/keys/public.pem
```

---

## 6. Code Triển Khai — Tích Hợp File 1 + File 2

### 6.1 RsaKeyProperties.java — Load Key

```java
// ===== config/RsaKeyProperties.java =====
// Spring Boot tự động đọc file .pem và convert sang RSA key objects

@Configuration
@ConfigurationProperties(prefix = "rsa")
@Getter @Setter
public class RsaKeyProperties {
    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;
}
```

```yaml
# application.yml
rsa:
  public-key: classpath:keys/public.pem
  private-key: classpath:keys/private.pem

app:
  jwt:
    access-token-expiration: 3600     # 1 giờ (giây)
    refresh-token-expiration: 604800  # 7 ngày
```

### 6.2 JwtConfig.java — Encoder, Decoder, Converter

> **Tách riêng** khỏi SecurityConfig — mỗi file một trách nhiệm.

#### Cách 1: RSA (Bất đối xứng — Production)

```java
// ===== config/JwtConfig.java =====

@Configuration
@RequiredArgsConstructor
public class JwtConfig {

    private final RsaKeyProperties rsaKeys;

    /**
     * JwtEncoder — TẠO token.
     * Cần Private Key để ký signature.
     */
    @Bean
    public JwtEncoder jwtEncoder() {
        // 1. Wrap RSA key pair thành JWK
        JWK jwk = new RSAKey.Builder(rsaKeys.getPublicKey())
            .privateKey(rsaKeys.getPrivateKey())
            .build();

        // 2. Tạo JWKSource từ JWKSet
        JWKSource<SecurityContext> jwkSource =
            new ImmutableJWKSet<>(new JWKSet(jwk));

        // 3. Tạo encoder dùng Nimbus library
        return new NimbusJwtEncoder(jwkSource);
    }

    /**
     * JwtDecoder — VERIFY + PARSE token.
     * Chỉ cần Public Key để verify signature.
     */
    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder
            .withPublicKey(rsaKeys.getPublicKey())
            .build();
    }

    /**
     * JwtAuthenticationConverter — Map JWT claims → Spring Authorities.
     * Quyết định claim nào trong JWT sẽ thành GrantedAuthority.
     */
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter grantedAuthorities =
            new JwtGrantedAuthoritiesConverter();

        // Đọc authorities từ claim "scope"
        grantedAuthorities.setAuthoritiesClaimName("scope");

        // Không thêm prefix (vì đã có ROLE_ khi tạo token)
        grantedAuthorities.setAuthorityPrefix("");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(grantedAuthorities);
        return converter;
    }
}
```

#### Cách 2: HMAC (Đối xứng — Dev/Simple App)

```java
// ===== config/JwtConfig.java — HMAC version =====

@Configuration
public class JwtConfig {

    @Value("${app.jwt.secret}")
    private String secretKey;

    @Bean
    public JwtEncoder jwtEncoder() {
        // OctetSequenceKey = JWK cho symmetric key
        SecretKey key = new SecretKeySpec(
            Decoders.BASE64.decode(secretKey), "HmacSHA256");
        JWK jwk = new OctetSequenceKey.Builder(key)
            .algorithm(JWSAlgorithm.HS256)
            .build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        SecretKey key = new SecretKeySpec(
            Decoders.BASE64.decode(secretKey), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
        //                      ^^^^^^^^^^^ khác RSA: .withPublicKey()
    }

    // JwtAuthenticationConverter — GIỐNG HỆT RSA version
    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        // ... giống code ở trên
    }
}
```

#### Chọn Key Theo Profile (Dev=HMAC, Prod=RSA)

```java
@Configuration
public class JwtConfig {

    @Bean @Profile("prod")
    public JwtEncoder rsaEncoder(RsaKeyProperties rsa) {
        JWK jwk = new RSAKey.Builder(rsa.getPublicKey())
            .privateKey(rsa.getPrivateKey()).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean @Profile("prod")
    public JwtDecoder rsaDecoder(RsaKeyProperties rsa) {
        return NimbusJwtDecoder.withPublicKey(rsa.getPublicKey()).build();
    }

    @Bean @Profile("dev")
    public JwtEncoder hmacEncoder(@Value("${app.jwt.secret}") String secret) {
        SecretKey key = new SecretKeySpec(
            Decoders.BASE64.decode(secret), "HmacSHA256");
        JWK jwk = new OctetSequenceKey.Builder(key)
            .algorithm(JWSAlgorithm.HS256).build();
        return new NimbusJwtEncoder(new ImmutableJWKSet<>(new JWKSet(jwk)));
    }

    @Bean @Profile("dev")
    public JwtDecoder hmacDecoder(@Value("${app.jwt.secret}") String secret) {
        SecretKey key = new SecretKeySpec(
            Decoders.BASE64.decode(secret), "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(key).build();
    }
}
```

### 6.3 JwtService.java — Business Logic Tạo Token

> **Inject** `JwtEncoder` bean (đã tạo ở JwtConfig).
> **KHÔNG có `@Bean`** — chỉ business logic.
> **Dùng** `CustomUserDetails` từ File 2.

```java
// ===== security/JwtService.java =====

@Service
@RequiredArgsConstructor
public class JwtService {

    private final JwtEncoder jwtEncoder;  // Inject từ JwtConfig

    @Value("${app.jwt.access-token-expiration:3600}")
    private long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration:604800}")
    private long refreshTokenExpiration;

    /**
     * Tạo Access Token từ Authentication (sau login).
     * 
     * Claims bao gồm:
     * - sub: username
     * - scope: authorities (space-separated)
     * - type: "access" (phân biệt với refresh)
     * - userId, email: custom claims từ CustomUserDetails
     * - jti: unique ID cho blacklist
     */
    public String generateAccessToken(Authentication authentication) {
        Instant now = Instant.now();

        // Lấy authorities → join thành string "ROLE_ADMIN ROLE_USER user:read"
        String scope = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(accessTokenExpiration))
            .subject(authentication.getName())
            .id(UUID.randomUUID().toString())  // jti — cho blacklist
            .claim("scope", scope)
            .claim("type", "access");

        // Thêm custom claims từ CustomUserDetails (File 2)
        if (authentication.getPrincipal() instanceof CustomUserDetails cu) {
            builder.claim("userId", cu.getUserId());
            builder.claim("email", cu.getEmail());
        }

        return jwtEncoder.encode(JwtEncoderParameters.from(builder.build()))
            .getTokenValue();
    }

    /**
     * Tạo Refresh Token — ít claims hơn, expiry dài hơn.
     */
    public String generateRefreshToken(Authentication authentication) {
        Instant now = Instant.now();

        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(now)
            .expiresAt(now.plusSeconds(refreshTokenExpiration))
            .subject(authentication.getName())
            .id(UUID.randomUUID().toString())
            .claim("type", "refresh")
            .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims))
            .getTokenValue();
    }

    /** Overload: tạo từ UserDetails (cho register auto-login). */
    public String generateAccessToken(UserDetails userDetails) {
        String scope = userDetails.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.joining(" "));

        JwtClaimsSet.Builder builder = JwtClaimsSet.builder()
            .issuer("self")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(accessTokenExpiration))
            .subject(userDetails.getUsername())
            .id(UUID.randomUUID().toString())
            .claim("scope", scope)
            .claim("type", "access");

        if (userDetails instanceof CustomUserDetails cu) {
            builder.claim("userId", cu.getUserId());
            builder.claim("email", cu.getEmail());
        }

        return jwtEncoder.encode(JwtEncoderParameters.from(builder.build()))
            .getTokenValue();
    }
}
```

### 6.4 SecurityConfig.java — Cập Nhật Hoàn Chỉnh

> **Tích hợp**: File 1 (base) + File 2 (exception handlers) + File 3 (JWT)

```java
// ===== config/SecurityConfig.java — PHIÊN BẢN HOÀN CHỈNH =====

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // Từ JwtConfig (file này)
    private final JwtDecoder jwtDecoder;
    private final JwtAuthenticationConverter jwtAuthConverter;

    // Từ File 2 (Auth)
    private final CustomAuthEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // [File 5] CORS — sẽ thêm config chi tiết
            .cors(Customizer.withDefaults())

            // [File 1] CSRF — disable cho REST API + JWT
            .csrf(csrf -> csrf.disable())

            // [File 1] Session — stateless
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // [File 1] Authorization Rules
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            // ★ [File 3 — MỚI] JWT Resource Server
            // Spring tự động thêm BearerTokenAuthenticationFilter
            // → Parse "Bearer xxx" → JwtDecoder verify → set SecurityContext
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .decoder(jwtDecoder)
                    .jwtAuthenticationConverter(jwtAuthConverter)
                )
            )

            // ★ [File 2 — MỚI] Exception Handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)
                .accessDeniedHandler(accessDeniedHandler)
            );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

### 6.5 AuthController.java — Login, Register, Refresh

```java
// ===== controller/AuthController.java =====

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager; // File 1
    private final JwtService jwtService;    // File 3
    private final JwtDecoder jwtDecoder;    // JwtConfig
    private final UserDetailsService userDetailsService; // File 2
    private final UserService userService;

    /**
     * POST /api/auth/login
     * 
     * Luồng:
     * 1. AuthenticationManager xác thực username/password
     *    → DaoAuthenticationProvider → UserDetailsService → PasswordEncoder
     * 2. JwtService tạo access + refresh token từ Authentication
     * 3. Trả tokens cho client
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {

        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(
                request.getUsername(),
                request.getPassword()
            )
        );

        String accessToken = jwtService.generateAccessToken(auth);
        String refreshToken = jwtService.generateRefreshToken(auth);

        return ResponseEntity.ok(AuthResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(3600)
            .build());
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        UserDetails userDetails = userService.createUser(request);

        String accessToken = jwtService.generateAccessToken(userDetails);
        String refreshToken = jwtService.generateRefreshToken(
            new UsernamePasswordAuthenticationToken(userDetails, null,
                userDetails.getAuthorities())
        );

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(3600)
                .build());
    }

    /**
     * POST /api/auth/refresh
     * 
     * Dùng JwtDecoder verify refresh token → tạo access token mới.
     */
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @RequestBody RefreshTokenRequest request) {

        Jwt jwt = jwtDecoder.decode(request.getRefreshToken());

        if (!"refresh".equals(jwt.getClaimAsString("type"))) {
            throw new UnauthorizedException("Invalid token type");
        }

        UserDetails userDetails = userDetailsService
            .loadUserByUsername(jwt.getSubject());

        String newAccessToken = jwtService.generateAccessToken(userDetails);

        return ResponseEntity.ok(AuthResponse.builder()
            .accessToken(newAccessToken)
            .refreshToken(request.getRefreshToken())
            .tokenType("Bearer")
            .expiresIn(3600)
            .build());
    }
}
```

### 6.6 DTOs

```java
@Data
public class LoginRequest {
    @NotBlank private String username;
    @NotBlank private String password;
}

@Data
public class RegisterRequest {
    @NotBlank @Size(min = 3, max = 50) private String username;
    @NotBlank @Size(min = 6) private String password;
    @Email private String email;
}

@Data
public class RefreshTokenRequest {
    @NotBlank private String refreshToken;
}

@Data @Builder
public class AuthResponse {
    private String accessToken;
    private String refreshToken;
    private String tokenType;
    private long expiresIn;
}
```

---

## 7. Truy Cập JWT Claims — Trong Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    // @AuthenticationPrincipal Jwt — truy cập claims trực tiếp
    @GetMapping("/me")
    public ResponseEntity<?> me(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(Map.of(
            "username", jwt.getSubject(),          // claim "sub"
            "userId", jwt.getClaimAsString("userId"),
            "scope", jwt.getClaimAsString("scope"),
            "expiresAt", jwt.getExpiresAt()        // claim "exp"
        ));
    }

    // JwtAuthenticationToken — có cả authorities đã map
    @GetMapping("/me2")
    public ResponseEntity<?> me2(JwtAuthenticationToken auth) {
        return ResponseEntity.ok(Map.of(
            "name", auth.getName(),
            "authorities", auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority).toList()
        ));
    }
}
```

---

## 8. Custom Validator & Blacklist

### 8.1 Composable Validators

```java
// Trong JwtConfig — thay thế jwtDecoder() đơn giản
@Bean
public JwtDecoder jwtDecoder(TokenBlacklistService blacklist) {
    NimbusJwtDecoder decoder = NimbusJwtDecoder
        .withPublicKey(rsaKeys.getPublicKey()).build();

    decoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
        // 1. Default: check exp, iat
        JwtValidators.createDefaultWithIssuer("self"),

        // 2. Check token type
        token -> {
            String type = token.getClaimAsString("type");
            if (!"access".equals(type) && !"refresh".equals(type)) {
                return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("invalid_type"));
            }
            return OAuth2TokenValidatorResult.success();
        },

        // 3. Blacklist check (cho logout)
        token -> blacklist.isBlacklisted(token.getId())
            ? OAuth2TokenValidatorResult.failure(new OAuth2Error("revoked"))
            : OAuth2TokenValidatorResult.success()
    ));
    return decoder;
}
```

### 8.2 Custom Claims Mapping

```java
// Thay thế JwtAuthenticationConverter mặc định
@Bean
public JwtAuthenticationConverter jwtAuthenticationConverter() {
    JwtAuthenticationConverter converter = new JwtAuthenticationConverter();

    converter.setJwtGrantedAuthoritiesConverter(jwt -> {
        List<GrantedAuthority> authorities = new ArrayList<>();

        // Đọc scope (space-separated)
        String scope = jwt.getClaimAsString("scope");
        if (scope != null) {
            Arrays.stream(scope.split(" "))
                .map(SimpleGrantedAuthority::new)
                .forEach(authorities::add);
        }

        // Đọc roles array (nếu có)
        List<String> roles = jwt.getClaimAsStringList("roles");
        if (roles != null) {
            roles.stream()
                .map(r -> new SimpleGrantedAuthority("ROLE_" + r))
                .forEach(authorities::add);
        }

        return authorities;
    });

    return converter;
}
```

---

## 9. Luồng Hoàn Chỉnh

```
LOGIN:
POST /api/auth/login {username, password}
    → AuthenticationManager.authenticate()          [SecurityConfig]
    → DaoAuthenticationProvider                      [Spring auto]
    → CustomUserDetailsService.loadUserByUsername()  [File 2]
    → PasswordEncoder.matches()                     [SecurityConfig]
    → JwtService.generateAccessToken(auth)          [File 3]
    → JwtEncoder.encode(claims)                     [JwtConfig]
    → Response: {accessToken: "eyJ...", refreshToken: "eyJ..."}

SUBSEQUENT REQUESTS:
GET /api/users/me (Authorization: Bearer eyJ...)
    → BearerTokenAuthenticationFilter               [Spring auto]
    → JwtDecoder.decode(token)                      [JwtConfig]
    → JwtAuthenticationConverter → authorities       [JwtConfig]
    → SecurityContext = authenticated                [Spring auto]
    → AuthorizationFilter: check rules              [SecurityConfig]
    → @PreAuthorize check                           [File 2]
    → Controller → Service → Response
```

---

## ✅ Checklist

- [ ] Hiểu JWT structure (Header, Payload, Signature)
- [ ] Phân biệt RSA vs HMAC, chọn theo use case
- [ ] Hiểu JWK, JwtEncoder, JwtDecoder, JwtAuthenticationConverter
- [ ] Tạo RsaKeyProperties + JwtConfig (tách riêng SecurityConfig)
- [ ] JwtService chỉ business logic, inject JwtEncoder bean
- [ ] SecurityConfig dùng .oauth2ResourceServer() — KHÔNG viết custom filter
- [ ] AuthController: login, register, refresh
- [ ] Custom validators (blacklist, type check)

---

> **Tiếp theo**: Đọc `Phase4.4_OAuth2.md` →
