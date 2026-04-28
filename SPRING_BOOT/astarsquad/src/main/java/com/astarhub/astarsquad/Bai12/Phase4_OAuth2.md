# 🌐 OAuth2 — Social Login & Resource Server

> **Tích hợp từ**: File 1-3 (SecurityConfig + UserDetails + JWT)
> **File này tạo**: OAuth2UserService, OAuth2SuccessHandler, User entity mở rộng
> **Ref**: [OAuth2 Login](https://docs.spring.io/spring-security/reference/servlet/oauth2/login/)

---

## 📑 Mục Lục

- [1. OAuth2 Là Gì?](#1-oauth2-là-gì)
  - [1.1 Các Vai Trò](#11-các-vai-trò)
  - [1.2 Authorization Code Flow](#12-authorization-code-flow--luồng-phổ-biến-nhất)
- [2. OAuth2 Login (Social Login)](#2-oauth2-login-social-login)
  - [2.1 Dependency](#21-dependency)
  - [2.2 application.yml](#22-applicationyml)
  - [2.3 User Entity — Mở Rộng Từ File 2](#23-user-entity--mở-rộng-từ-file-2)
  - [2.4 CustomOAuth2UserService](#24-customoauth2userservice--xử-lý-user-từ-provider)
  - [2.5 CustomOAuth2User](#25-customoauth2user)
  - [2.6 OAuth2 Login → Tạo JWT](#26-oauth2-login--tạo-jwt)
  - [2.7 SecurityConfig — Tích Hợp](#27-securityconfig--tích-hợp-oauth2-login)
- [3. OAuth2 Resource Server](#3-oauth2-resource-server--api-nhận-jwt-từ-bên-ngoài)
- [4. Khi Nào Dùng Gì?](#4-khi-nào-dùng-gì)
- [✅ Checklist](#-checklist)

---

## 1. OAuth2 Là Gì?

**OAuth2** = giao thức ủy quyền (authorization framework) cho phép ứng dụng 
truy cập tài nguyên của user trên dịch vụ khác **mà không cần biết password**.

### 1.1 Các Vai Trò

| Vai trò | Là ai | Ví dụ |
|---------|-------|-------|
| **Resource Owner** | User — người sở hữu data | Bạn (có tài khoản Google) |
| **Client** | App muốn truy cập data của user | App của bạn (Spring Boot) |
| **Authorization Server** | Server cấp phát token | Google, GitHub, Keycloak |
| **Resource Server** | API bảo vệ data bằng token | API backend của bạn |

### 1.2 Authorization Code Flow — Luồng Phổ Biến Nhất

```
1. User click "Login with Google" trên app
        │
        ▼
2. App redirect user → Google Authorization Server
   https://accounts.google.com/o/oauth2/auth
   ?client_id=xxx            ← App ID đăng ký với Google
   &redirect_uri=http://localhost:8080/login/oauth2/code/google
   &scope=openid profile email
   &response_type=code       ← Yêu cầu authorization code
        │
        ▼
3. User đăng nhập Google + đồng ý chia sẻ thông tin
        │
        ▼
4. Google redirect về app kèm authorization code
   http://localhost:8080/login/oauth2/code/google?code=ABC123
        │
        ▼
5. Backend gửi code → Google để đổi lấy access_token
   (Server-to-server, KHÔNG qua browser → an toàn)
   POST https://oauth2.googleapis.com/token
   {code: "ABC123", client_id, client_secret}
        │
        ▼
6. Google trả về access_token
        │
        ▼
7. Backend dùng access_token gọi Google API lấy user info
   GET https://www.googleapis.com/oauth2/v3/userinfo
   → {email, name, picture}
        │
        ▼
8. Backend tạo/update user trong DB → tạo JWT nội bộ → trả cho client
```

> **Tại sao dùng Authorization Code thay vì gửi password?**
> - User KHÔNG bao giờ nhập password Google trên app của bạn
> - App KHÔNG biết password Google
> - User có thể revoke quyền bất kỳ lúc nào

---

## 2. OAuth2 Login (Social Login)

### 2.1 Dependency

```xml
<!-- Thêm vào cùng oauth2-resource-server từ File 3 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-oauth2-client</artifactId>
</dependency>
```

### 2.2 application.yml

```yaml
spring:
  security:
    oauth2:
      client:
        registration:
          google:
            client-id: ${GOOGLE_CLIENT_ID}      # Lấy từ Google Cloud Console
            client-secret: ${GOOGLE_CLIENT_SECRET}
            scope: openid, profile, email

          github:
            client-id: ${GITHUB_CLIENT_ID}      # Lấy từ GitHub Developer Settings
            client-secret: ${GITHUB_CLIENT_SECRET}
            scope: user:email, read:user
```

> **Lấy client-id/secret ở đâu?**
> - Google: https://console.cloud.google.com → APIs & Services → Credentials
> - GitHub: https://github.com/settings/developers → OAuth Apps

### 2.3 User Entity — Mở Rộng Từ File 2

```java
// ===== entity/User.java — THÊM OAuth2 fields =====
// Thêm vào User entity đã tạo ở File 2

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;
    private String email;
    private String password;       // NULL nếu OAuth2 user (không có password)

    // ★ THÊM MỚI cho OAuth2
    @Enumerated(EnumType.STRING)
    private AuthProvider provider;  // LOCAL, GOOGLE, GITHUB

    private String providerId;     // ID từ OAuth2 provider (Google sub, GitHub id)

    private boolean active = true;
    private boolean locked = false;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();
}

public enum AuthProvider {
    LOCAL,   // Đăng ký bằng username/password
    GOOGLE,  // Login bằng Google
    GITHUB   // Login bằng GitHub
}
```

### 2.4 CustomOAuth2UserService — Xử Lý User Từ Provider

```java
// ===== security/CustomOAuth2UserService.java =====

/**
 * Được gọi SAU KHI Spring đổi code → access_token → gọi userinfo endpoint.
 * Nhận OAuth2User (thông tin user từ Google/GitHub) → tạo/update user trong DB.
 */
@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest)
            throws OAuth2AuthenticationException {

        // 1. Gọi parent → lấy user info từ provider
        OAuth2User oAuth2User = super.loadUser(userRequest);

        // 2. Xác định provider nào
        String registrationId = userRequest.getClientRegistration()
            .getRegistrationId();  // "google" hoặc "github"

        // 3. Extract thông tin (mỗi provider trả format khác nhau)
        String email, name, providerId;

        if ("google".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("name");
            providerId = oAuth2User.getAttribute("sub");
        } else if ("github".equals(registrationId)) {
            email = oAuth2User.getAttribute("email");
            name = oAuth2User.getAttribute("login");
            providerId = String.valueOf(
                (Integer) oAuth2User.getAttribute("id"));
        } else {
            throw new OAuth2AuthenticationException("Unsupported provider");
        }

        // 4. Tạo hoặc update user trong DB
        User user = userRepository.findByEmail(email)
            .map(existingUser -> {
                // User đã tồn tại → update info
                existingUser.setUsername(name);
                existingUser.setProvider(
                    AuthProvider.valueOf(registrationId.toUpperCase()));
                existingUser.setProviderId(providerId);
                return userRepository.save(existingUser);
            })
            .orElseGet(() -> {
                // User mới → tạo với role USER
                Role userRole = roleRepository.findByName("USER")
                    .orElseThrow();
                User newUser = User.builder()
                    .email(email)
                    .username(name)
                    .provider(AuthProvider.valueOf(
                        registrationId.toUpperCase()))
                    .providerId(providerId)
                    .active(true)
                    .roles(Set.of(userRole))
                    .build();
                // password = null (OAuth2 user không cần password)
                return userRepository.save(newUser);
            });

        // 5. Wrap thành CustomOAuth2User
        return new CustomOAuth2User(user, oAuth2User.getAttributes());
    }
}
```

### 2.5 CustomOAuth2User

```java
// ===== security/CustomOAuth2User.java =====

/**
 * Implement cả OAuth2User VÀ UserDetails
 * → dùng được cho cả OAuth2 login và JWT generation.
 */
public class CustomOAuth2User implements OAuth2User, UserDetails {

    private final User user;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    // ===== OAuth2User methods =====
    @Override
    public Map<String, Object> getAttributes() { return attributes; }

    @Override
    public String getName() { return user.getUsername(); }

    // ===== UserDetails methods =====
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // Dùng lại logic từ CustomUserDetails (File 2)
        List<GrantedAuthority> authorities = new ArrayList<>();
        for (Role role : user.getRoles()) {
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            for (Permission perm : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(perm.getName()));
            }
        }
        return authorities;
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getUsername(); }

    // Custom
    public Long getUserId() { return user.getId(); }
    public String getEmail() { return user.getEmail(); }
}
```

### 2.6 OAuth2 Login → Tạo JWT

```java
// ===== security/OAuth2LoginSuccessHandler.java =====

/**
 * Sau khi OAuth2 login thành công → tạo JWT → redirect về frontend.
 * Dùng JwtService từ File 3.
 */
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler
        extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;  // Từ File 3

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication) throws IOException {

        CustomOAuth2User oAuth2User =
            (CustomOAuth2User) authentication.getPrincipal();

        // Dùng JwtService tạo token (giống login thường)
        String accessToken = jwtService.generateAccessToken(authentication);
        String refreshToken = jwtService.generateRefreshToken(authentication);

        // Redirect về frontend kèm tokens
        String targetUrl = UriComponentsBuilder
            .fromUriString("http://localhost:3000/oauth2/callback")
            .queryParam("access_token", accessToken)
            .queryParam("refresh_token", refreshToken)
            .build().toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}
```

### 2.7 SecurityConfig — Tích Hợp OAuth2 Login

```java
// ===== Thêm vào SecurityConfig (cập nhật từ File 3) =====

@Bean
public SecurityFilterChain securityFilterChain(HttpSecurity http)
        throws Exception {

    http
        // ... giữ nguyên cors, csrf, session, authorizeHttpRequests
        // ... giữ nguyên oauth2ResourceServer (File 3)
        // ... giữ nguyên exceptionHandling (File 2)

        // ★ THÊM MỚI — OAuth2 Login
        .oauth2Login(oauth2 -> oauth2
            .userInfoEndpoint(userInfo ->
                userInfo.userService(customOAuth2UserService)
            )
            .successHandler(oAuth2LoginSuccessHandler)
        );

    return http.build();
}
```

---

## 3. OAuth2 Resource Server — API Nhận JWT Từ Bên Ngoài

> Khi app của bạn **KHÔNG tự tạo JWT** mà nhận JWT từ **Authorization Server bên ngoài**
> (Keycloak, Auth0, Okta).

```java
// application.yml — chỉ cần issuer-uri
spring:
  security:
    oauth2:
      resourceserver:
        jwt:
          issuer-uri: https://your-keycloak.com/realms/your-realm
          # Spring tự động fetch public key từ issuer

// SecurityConfig — giống File 3, chỉ khác không cần JwtConfig
// vì Spring auto-config JwtDecoder từ issuer-uri
```

---

## 4. Khi Nào Dùng Gì?

| Tình huống | Giải pháp | File tham khảo |
|-----------|-----------|---------------|
| REST API + SPA | JWT tự tạo (RSA) | File 3 |
| Cần Google/GitHub login | OAuth2 Client + JWT | File 3 + File 4 |
| Microservices + Auth Server chung | OAuth2 Resource Server | File 4 section 3 |
| Enterprise, SSO | Keycloak/Auth0 | File 4 section 3 |
| App nhỏ, ít user | JWT tự tạo là đủ | File 3 |

---

## ✅ Checklist

- [ ] Hiểu OAuth2 roles (Resource Owner, Client, Auth Server, Resource Server)
- [ ] Hiểu Authorization Code Flow (7 bước)
- [ ] Config Google/GitHub trong application.yml
- [ ] CustomOAuth2UserService: extract info → tạo/update user
- [ ] OAuth2LoginSuccessHandler: tạo JWT → redirect frontend
- [ ] Phân biệt OAuth2 Client vs OAuth2 Resource Server

---

> **Tiếp theo**: Đọc `Phase4_BestPractice_Password_CORS_CSRF.md` →
