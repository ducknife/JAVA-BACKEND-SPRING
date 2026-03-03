# Bài 12.3: OAuth2 — Đăng Nhập Qua Bên Thứ Ba

> **Stack**: Spring Boot 3.4.3 · Spring Security 6.4.x · OAuth2 Client

## 1. OAuth2 Là Gì?

**OAuth2** (Open Authorization 2.0) là giao thức ủy quyền cho phép một ứng dụng truy cập tài nguyên của người dùng trên một dịch vụ khác mà **không cần biết mật khẩu** của họ.

### Ví dụ thực tế:
- "Đăng nhập bằng Google" trên Shopee
- "Đăng nhập bằng GitHub" trên nhiều developer tools
- App đọc lịch Google Calendar của bạn

---

## 2. Các Khái Niệm Quan Trọng

| Thuật ngữ | Giải thích |
|-----------|------------|
| **Resource Owner** | Người dùng (bạn) |
| **Client** | Ứng dụng của bạn (Spring Boot app) |
| **Authorization Server** | Server cấp token (Google, GitHub, ...) |
| **Resource Server** | Server chứa dữ liệu (Google API, GitHub API) |
| **Access Token** | Token để truy cập Resource Server |
| **Authorization Code** | Mã tạm thời để đổi lấy Access Token |

---

## 3. OAuth2 Authorization Code Flow

Đây là flow phổ biến nhất và an toàn nhất:

```
User          Client App            Auth Server          Resource Server
  │                │                     │                      │
  │  Click "Login  │                     │                      │
  │  with Google"  │                     │                      │
  │ ─────────────► │                     │                      │
  │                │ Redirect to Google  │                      │
  │                │ (/oauth2/authorize) │                      │
  │                │ ────────────────►   │                      │
  │ Google login   │                     │                      │
  │ page hiện ra   │                     │                      │
  │ ◄────────────────────────────────── │                      │
  │                │                     │                      │
  │ User đồng ý   │                     │                      │
  │ ────────────────────────────────► │                      │
  │                │                     │                      │
  │                │ ◄── Redirect với   │                      │
  │                │     code=XYZ        │                      │
  │                │                     │                      │
  │                │ POST /token         │                      │
  │                │ code=XYZ            │                      │
  │                │ ────────────────► │                      │
  │                │                     │                      │
  │                │ ◄── access_token   │                      │
  │                │     refresh_token   │                      │
  │                │                     │                      │
  │                │ GET /userinfo       │                      │
  │                │ Bearer access_token │──────────────────►  │
  │                │                     │                      │
  │                │ ◄─────────────────────────── user info   │
  │                │                     │                      │
  │ ◄────────────  │                     │                      │
  │   Logged in    │                     │                      │
```

---

## 4. OAuth2 Trong Spring Boot

### 4.1 Thêm Dependency

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

### 4.2 Cấu Hình application.yml

#### Đăng nhập Google

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: YOUR_GOOGLE_CLIENT_ID
            client-secret: YOUR_GOOGLE_CLIENT_SECRET
            scope:
              - email
              - profile
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
```

#### Đăng nhập GitHub

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          github:
            client-id: YOUR_GITHUB_CLIENT_ID
            client-secret: YOUR_GITHUB_CLIENT_SECRET
            scope:
              - user:email
              - read:user
```

#### Cấu hình đầy đủ cả hai (chuẩn production)

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: email, profile
            # Spring Security 6.4 hỗ trợ PKCE cho confidential clients
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
          github:
            client-id: ${GITHUB_CLIENT_ID}
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: user:email, read:user
            authorization-grant-type: authorization_code
            redirect-uri: "{baseUrl}/login/oauth2/code/{registrationId}"
        provider:
          google:
            authorization-uri: https://accounts.google.com/o/oauth2/auth
            token-uri: https://oauth2.googleapis.com/token
            user-info-uri: https://www.googleapis.com/oauth2/v3/userinfo
            user-name-attribute: sub
          github:
            authorization-uri: https://github.com/login/oauth/authorize
            token-uri: https://github.com/login/oauth/access_token
            user-info-uri: https://api.github.com/user
            user-name-attribute: id
```

> ⚠️ **Lưu ý**: Không commit `client-id` / `client-secret` lên git. Dùng `.env` hoặc secret manager.

---

## 5. Lấy Client ID & Secret

### Google
1. Vào [Google Cloud Console](https://console.cloud.google.com/)
2. Tạo project → **APIs & Services** → **Credentials**
3. **Create Credentials** → **OAuth 2.0 Client IDs**
4. Application type: **Web application**
5. Thêm redirect URI: `http://localhost:8080/login/oauth2/code/google`
6. Copy Client ID và Client Secret

### GitHub
1. Vào **Settings** → **Developer settings** → **OAuth Apps**
2. **New OAuth App**
3. Authorization callback URL: `http://localhost:8080/login/oauth2/code/github`
4. Copy Client ID và generate Client Secret

---

## 6. SecurityConfig Với OAuth2

### 6.1 Cơ Bản (Redirect về trang login)

```java
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/public/**").permitAll()
                .anyRequest().authenticated()
            )
            // Kích hoạt OAuth2 Login
            .oauth2Login(oauth2 -> oauth2
                .loginPage("/login")
                .defaultSuccessUrl("/dashboard", true)
                .failureUrl("/login?error=true")
            );

        return http.build();
    }
}
```

### 6.2 Kết Hợp JWT + OAuth2 (REST API)

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final OAuth2UserServiceImpl oAuth2UserService;
    private final OAuth2AuthenticationSuccessHandler successHandler;
    private final OAuth2AuthenticationFailureHandler failureHandler;
    private final JwtAuthenticationFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**", "/oauth2/**").permitAll()
                .anyRequest().authenticated()
            )
            // Cấu hình OAuth2 Login
            .oauth2Login(oauth2 -> oauth2
                .authorizationEndpoint(endpoint -> endpoint
                    .baseUri("/oauth2/authorize"))
                .redirectionEndpoint(endpoint -> endpoint
                    .baseUri("/login/oauth2/code/*"))
                .userInfoEndpoint(userInfo -> userInfo
                    .userService(oAuth2UserService))     // Xử lý user info
                .successHandler(successHandler)          // Tạo JWT sau khi login OK
                .failureHandler(failureHandler)          // Xử lý lỗi
            )
            .addFilterBefore(jwtAuthFilter,
                UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
```

---

## 7. OAuth2UserService — Xử Lý User Info

```java
// security/oauth2/OAuth2UserServiceImpl.java
@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2UserServiceImpl extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        OAuth2User oAuth2User = super.loadUser(userRequest);

        String provider = userRequest.getClientRegistration()
            .getRegistrationId();   // "google" | "github"

        // Dùng sealed interface factory (Java 17)
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(
            provider, oAuth2User.getAttributes());

        if (userInfo.getEmail() == null || userInfo.getEmail().isBlank()) {
            throw new OAuth2AuthenticationException(
                "Email không tìm thấy từ provider " + provider);
        }

        // Upsert user: tìm theo email, nếu không có thì tạo mới
        User user = userRepository.findByEmail(userInfo.getEmail())
            .map(existing -> updateExistingUser(existing, userInfo))
            .orElseGet(() -> registerNewUser(provider, userInfo));

        return UserPrincipal.create(user);
    }

    private User registerNewUser(String provider, OAuth2UserInfo info) {
        log.info("Đăng ký user mới qua {}: {}", provider, info.getEmail());

        var user = new User();
        user.setProvider(AuthProvider.valueOf(provider.toUpperCase()));
        user.setProviderId(info.getId());
        user.setName(info.getName());
        user.setEmail(info.getEmail());
        user.setImageUrl(info.getImageUrl());
        user.setPassword(null);                 // null = không dùng local login
        user.setRoles(Set.of(/* findRole(ROLE_USER) */));
        return userRepository.save(user);
    }

    private User updateExistingUser(User user, OAuth2UserInfo info) {
        user.setName(info.getName());
        user.setImageUrl(info.getImageUrl());
        return userRepository.save(user);
    }
}
```

### OAuth2UserInfo — Sealed Interface (Java 17)

> **Senior note**: Dùng `sealed interface` + `record` thay cho abstract class. Code ngắn hơn, type-safe hơn, không cần class riêng.

```java
// security/oauth2/userinfo/OAuth2UserInfo.java
/**
 * Sealed interface — chỉ 3 implementation được phép, compiler biết hết.
 * Kết hợp với pattern matching switch (Java 21) rất clean.
 */
public sealed interface OAuth2UserInfo
    permits GoogleOAuth2UserInfo, GithubOAuth2UserInfo, FacebookOAuth2UserInfo {

    String getId();
    String getName();
    String getEmail();
    String getImageUrl();

    /** Factory — chọn implementation dựa vào registrationId */
    static OAuth2UserInfo of(String registrationId,
                              Map<String, Object> attributes) {
        return switch (registrationId.toLowerCase()) {
            case "google"   -> new GoogleOAuth2UserInfo(attributes);
            case "github"   -> new GithubOAuth2UserInfo(attributes);
            case "facebook" -> new FacebookOAuth2UserInfo(attributes);
            default -> throw new OAuth2AuthenticationException(
                "Provider '" + registrationId + "' chưa được hỗ trợ");
        };
    }
}

// Google — dùng record (immutable, auto getter)
public record GoogleOAuth2UserInfo(Map<String, Object> attributes)
        implements OAuth2UserInfo {
    @Override public String getId()       { return (String) attributes.get("sub"); }
    @Override public String getName()     { return (String) attributes.get("name"); }
    @Override public String getEmail()    { return (String) attributes.get("email"); }
    @Override public String getImageUrl() { return (String) attributes.get("picture"); }
}

// GitHub
public record GithubOAuth2UserInfo(Map<String, Object> attributes)
        implements OAuth2UserInfo {
    @Override public String getId()       { return String.valueOf(attributes.get("id")); }
    @Override public String getName()     { return (String) attributes.get("name"); }
    @Override public String getEmail()    { return (String) attributes.get("email"); }
    @Override public String getImageUrl() { return (String) attributes.get("avatar_url"); }
}

// Facebook (placeholder)
public record FacebookOAuth2UserInfo(Map<String, Object> attributes)
        implements OAuth2UserInfo {
    @Override public String getId()       { return (String) attributes.get("id"); }
    @Override public String getName()     { return (String) attributes.get("name"); }
    @Override public String getEmail()    { return (String) attributes.get("email"); }
    @Override public String getImageUrl() {
        var picture = (Map<?, ?>) attributes.get("picture");
        if (picture == null) return null;
        var data = (Map<?, ?>) picture.get("data");
        return data == null ? null : (String) data.get("url");
    }
}
```

---

## 8. Success Handler — Tạo JWT Sau OAuth2 Login

```java
// security/oauth2/OAuth2AuthenticationSuccessHandler.java
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService         jwtService;
    private final RefreshTokenService refreshTokenService;
    private final UserRepository      userRepository;
    private final ObjectMapper        objectMapper;      // inject Spring's Bean

    @Value("${app.oauth2.redirect-uri:http://localhost:3000/oauth2/callback}")
    private String frontendRedirectUri;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest  request,
                                         HttpServletResponse response,
                                         Authentication      authentication)
            throws IOException {

        var principal    = (UserPrincipal) authentication.getPrincipal();
        var accessToken  = jwtService.generateAccessToken(principal);
        var user         = userRepository.findById(principal.getId()).orElseThrow();
        var refreshToken = refreshTokenService.create(user.getId());

        List<String> roles = principal.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority).toList();

        var authResponse = new AuthResponse(
            accessToken, refreshToken.getId(),
            jwtService.getAccessExpirationMs(),
            principal.getUsername(), roles
        );

        // ── Lựa chọn 1: REST API — trả JSON ──
        if (isApiRequest(request)) {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setStatus(HttpServletResponse.SC_OK);
            objectMapper.writeValue(response.getWriter(), authResponse);
            return;
        }

        // ── Lựa chọn 2: SPA (React/Vue) — redirect về frontend kèm token ──
        String redirectUri = UriComponentsBuilder
            .fromUriString(frontendRedirectUri)
            .queryParam("accessToken", accessToken)
            .queryParam("refreshToken", refreshToken.getId())
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUri);
    }

    private boolean isApiRequest(HttpServletRequest request) {
        String accept = request.getHeader(HttpHeaders.ACCEPT);
        return accept != null && accept.contains(MediaType.APPLICATION_JSON_VALUE);
    }
}
```

---

## 9. Failure Handler

```java
@Component
public class OAuth2AuthenticationFailureHandler
        extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception) throws IOException {

        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.getWriter().write(
            "{\"error\": \"OAuth2 authentication failed: "
            + exception.getMessage() + "\"}"
        );
    }
}
```

---

## 10. Entity User Hỗ Trợ OAuth2

```java
@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String password; // null nếu dùng OAuth2

    private String imageUrl;

    @Enumerated(EnumType.STRING)
    private AuthProvider provider; // LOCAL, GOOGLE, GITHUB

    private String providerId; // ID từ Google/GitHub

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    private Set<ERole> roles;
}

// AuthProvider.java
public enum AuthProvider {
    LOCAL, GOOGLE, GITHUB, FACEBOOK
}
```

---

## 11. Tóm Tắt

```
OAuth2 Flow:
1. User click "Login với Google"
2. Redirect đến Google → User đăng nhập & đồng ý
3. Google redirect về với code
4. Spring Security đổi code → access_token
5. Lấy user info từ Google
6. Tạo/update User trong DB
7. Tạo JWT → trả về client

Components:
- application.yml      → Cấu hình provider (client-id, secret)
- OAuth2UserService    → Xử lý user info, tạo/update DB
- OAuth2UserInfoFactory→ Normalize thông tin từ các provider khác nhau
- SuccessHandler       → Tạo JWT sau khi OAuth2 login thành công
- FailureHandler       → Xử lý lỗi
```

---

## 12. Bài Tập Thực Hành

1. Tạo Google OAuth App và GitHub OAuth App
2. Thêm `spring-boot-starter-oauth2-client` vào project
3. Cấu hình `application.yml` với credentials
4. Implement `OAuth2UserService` lưu user vào DB
5. Implement `SuccessHandler` tạo JWT và trả về
6. Test flow đăng nhập qua Google từ Postman/browser

> Tiếp theo: [04_Best_Practices.md](04_Best_Practices.md) — Best Practices bảo mật
