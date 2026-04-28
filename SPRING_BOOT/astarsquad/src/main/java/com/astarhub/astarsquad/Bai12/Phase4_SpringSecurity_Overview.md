# 📌 Phase 4: Spring Security — Tổng Quan Kiến Trúc

> **Nguồn**: [Spring Security Reference](https://docs.spring.io/spring-security/reference/)
> **Version**: Spring Boot 3.x + Spring Security 6.x/7.x

---

## 📖 Mục Lục Phase 4

| # | File | Nội dung | Ghi chú |
|---|------|---------|---------|
| 1 | `Phase4_SpringSecurity_Overview.md` | Kiến trúc + khái niệm nền tảng | **File này** — đọc đầu tiên |
| 2 | `Phase4_Authentication_vs_Authorization.md` | Xác thực & phân quyền chi tiết | Tích hợp code từ file 1 |
| 3 | `Phase4_JWT_Token.md` | JWT với oauth2-resource-server | Tích hợp code từ file 1+2 |
| 4 | `Phase4_OAuth2.md` | Social Login + Resource Server | Tích hợp code từ file 1+2+3 |
| 5 | `Phase4_BestPractice_Password_CORS_CSRF.md` | BCrypt, CORS, CSRF | Tích hợp vào SecurityConfig chung |

> Các file tích hợp dần — code ở file sau **kế thừa và mở rộng** code ở file trước.

---

## 1. Spring Security Là Gì?

**Spring Security** là framework bảo mật cho ứng dụng Java/Spring. Nó cung cấp:

- **Authentication** (Xác thực): Xác minh danh tính người dùng — "Bạn là ai?"
- **Authorization** (Phân quyền): Kiểm soát quyền truy cập — "Bạn được làm gì?"
- **Protection**: Chống các tấn công phổ biến (CSRF, XSS, Session Fixation, Clickjacking)

### Tại sao cần Spring Security?

```
Không có Spring Security:
Client → Controller → Service → Database
         ↑ BẤT KỲ AI cũng gọi được!

Có Spring Security:
Client → [Filter Chain] → Controller → Service → Database
          ↑ Kiểm tra: Ai? Có quyền không? Token hợp lệ?
```

---

## 2. Kiến Trúc Tổng Quan — Servlet Filter Chain

### 2.1 Khái niệm: Servlet Filter

**Filter** là thành phần nằm giữa Client và Servlet (Controller).
Mỗi HTTP request đi qua một **chuỗi filter** trước khi đến DispatcherServlet.

Spring Security **chen toàn bộ logic bảo mật vào filter chain** này.

```
Client HTTP Request
    │
    ▼
┌─────────────────────────────────────────────────────────┐
│                  Servlet Container (Tomcat)              │
│                                                         │
│   Filter 1 (Logging)                                    │
│       │                                                 │
│       ▼                                                 │
│   DelegatingFilterProxy ← (1) Cầu nối Servlet → Spring │
│       │                                                 │
│       ▼                                                 │
│   FilterChainProxy      ← (2) Spring Security quản lý  │
│       │                                                 │
│       ▼                                                 │
│   SecurityFilterChain   ← (3) Chuỗi 15+ security filter│
│       │                                                 │
│       ▼                                                 │
│   Filter N (Other)                                      │
│       │                                                 │
│       ▼                                                 │
│   DispatcherServlet → Controller                        │
└─────────────────────────────────────────────────────────┘
```

### 2.2 Ba Thành Phần Chính

#### (1) DelegatingFilterProxy — Cầu Nối

```
Vấn đề: Servlet Container (Tomcat) không biết Spring Bean.
         Spring Bean không phải Servlet Filter.

Giải pháp: DelegatingFilterProxy là Servlet Filter THẬT
           → delegate (ủy quyền) xử lý cho Spring Bean.

Servlet Container đăng ký:  DelegatingFilterProxy (tên = "springSecurityFilterChain")
                                    │
                                    │ delegate
                                    ▼
                            FilterChainProxy (Spring Bean)
```

**Bạn KHÔNG cần cấu hình gì** — Spring Boot auto-config `DelegatingFilterProxy` khi thêm dependency `spring-boot-starter-security`.

#### (2) FilterChainProxy — Bộ Điều Phối

```
FilterChainProxy chứa nhiều SecurityFilterChain.
Khi request đến, nó chọn chain PHÙ HỢP đầu tiên (theo URL pattern).

FilterChainProxy
    ├── SecurityFilterChain [/api/**]     → JWT authentication
    ├── SecurityFilterChain [/admin/**]   → Form login
    └── SecurityFilterChain [/**]         → Default (deny all)
```

**Tại sao cần FilterChainProxy thay vì dùng Servlet Filter trực tiếp?**
- Có thể có **nhiều SecurityFilterChain** (multi-config)
- Là **điểm debug duy nhất** — breakpoint ở đây thấy toàn bộ security flow
- Tự động thêm các tác vụ bắt buộc (clear SecurityContext, apply firewall rules)

#### (3) SecurityFilterChain — Chuỗi Filter Bảo Mật

Đây là nơi logic bảo mật thực sự nằm. Mỗi chain gồm **nhiều filter có thứ tự cố định**:

```
SecurityFilterChain (thứ tự filter):

 #  Filter                              Nhiệm vụ
─── ──────────────────────────────────── ────────────────────────────────
 1  DisableEncodeUrlFilter               Chống session ID lộ qua URL
 2  WebAsyncManagerIntegrationFilter     Hỗ trợ async request
 3  SecurityContextHolderFilter          Load/save SecurityContext
 4  HeaderWriterFilter                   Thêm security headers (XSS, HSTS)
 5  CorsFilter                           Xử lý CORS preflight
 6  CsrfFilter                           Kiểm tra CSRF token
 7  LogoutFilter                         Xử lý logout request
 8  BearerTokenAuthenticationFilter ★    Parse JWT từ "Authorization: Bearer"
 9  UsernamePasswordAuthenticationFilter Xử lý form login POST
10  RequestCacheAwareFilter              Cache request trước redirect
11  SecurityContextHolderAwareRequestFilter  Wrap request thêm method
12  AnonymousAuthenticationFilter        Gán anonymous nếu chưa auth
13  ExceptionTranslationFilter           Bắt AuthException → 401/403
14  AuthorizationFilter ★                Kiểm tra quyền truy cập
─── ──────────────────────────────────── ────────────────────────────────
     ★ = Quan trọng nhất cho REST API + JWT
```

> **Thứ tự quan trọng!** Authentication filter (#8) chạy TRƯỚC Authorization filter (#14).
> Nếu chưa authenticate → không authorize được.

---

## 3. Các Khái Niệm Cốt Lõi

### 3.1 SecurityContext & SecurityContextHolder

**SecurityContext** = nơi lưu trữ thông tin user hiện tại (đã xác thực).

**SecurityContextHolder** = class static giữ SecurityContext, mặc định dùng **ThreadLocal** (mỗi thread một context riêng).

```
SecurityContextHolder
    │
    │  .getContext()
    ▼
SecurityContext
    │
    │  .getAuthentication()
    ▼
Authentication (interface)
    ├── getPrincipal()      → Object: thường là UserDetails (thông tin user)
    ├── getCredentials()    → Object: password (bị xóa sau khi xác thực)
    ├── getAuthorities()    → Collection<GrantedAuthority>: danh sách quyền
    ├── getDetails()        → Object: thông tin bổ sung (IP, session ID)
    └── isAuthenticated()   → boolean: đã xác thực chưa?
```

#### Giải thích từng thành phần:

| Thành phần | Là gì | Ví dụ |
|-----------|-------|-------|
| **Principal** | Đại diện cho user. Thường là object `UserDetails` hoặc `Jwt` | `CustomUserDetails{id=1, username="admin"}` |
| **Credentials** | Bằng chứng xác thực. Sau khi xác thực xong, Spring **xóa** để tránh lộ | Password "123456" → `null` sau auth |
| **Authorities** | Danh sách quyền dạng flat list, mỗi quyền là một string | `[ROLE_ADMIN, ROLE_USER, user:read]` |
| **Details** | Metadata về request. Thường là `WebAuthenticationDetails` | `{remoteAddress="192.168.1.1", sessionId="abc"}` |

#### Code minh họa — Lấy thông tin user hiện tại:

```java
// ===== Ở bất kỳ đâu trong code (Service, Util,...) =====

// Lấy SecurityContext từ ThreadLocal
SecurityContext context = SecurityContextHolder.getContext();

// Lấy Authentication object
Authentication authentication = context.getAuthentication();

// Kiểm tra đã xác thực chưa
if (authentication != null && authentication.isAuthenticated()) {
    
    // Lấy username
    String username = authentication.getName();
    
    // Lấy Principal (cast về UserDetails hoặc Jwt)
    Object principal = authentication.getPrincipal();
    
    if (principal instanceof UserDetails userDetails) {
        // Từ UserDetailsService
        String password = userDetails.getPassword(); // null (đã xóa)
        Collection<? extends GrantedAuthority> authorities = 
            userDetails.getAuthorities();
    }
    
    if (principal instanceof Jwt jwt) {
        // Từ OAuth2 Resource Server
        String userId = jwt.getClaimAsString("userId");
        Instant expiry = jwt.getExpiresAt();
    }
}
```

### 3.2 Authentication Interface

**Authentication** vừa là **input** (chứa credentials chưa xác thực), vừa là **output** (chứa user đã xác thực) của quá trình xác thực.

```java
// TRƯỚC xác thực: chứa credentials
Authentication unauthenticated = new UsernamePasswordAuthenticationToken(
    "admin",      // principal = username (String)
    "password123" // credentials = raw password
);
// isAuthenticated() = false
// authorities = empty

// SAU xác thực: chứa user details + authorities
Authentication authenticated = new UsernamePasswordAuthenticationToken(
    userDetails,   // principal = UserDetails object
    null,          // credentials = null (đã xóa)
    authorities    // authorities = [ROLE_ADMIN, user:read, ...]
);
// isAuthenticated() = true
```

### 3.3 GrantedAuthority — Quyền Hạn

**GrantedAuthority** = một đơn vị quyền. Spring Security phân biệt 2 loại:

```
GrantedAuthority (interface)
    │
    └── SimpleGrantedAuthority (implementation phổ biến nhất)
            │
            ├── Role:       "ROLE_ADMIN"      ← có prefix "ROLE_"
            │                                    dùng với hasRole("ADMIN")
            │                                    Spring TỰ ĐỘNG thêm prefix
            │
            └── Permission: "user:read"       ← không có prefix
                                                 dùng với hasAuthority("user:read")
```

| Loại | Cách lưu | Cách check | Ví dụ |
|------|---------|-----------|-------|
| **Role** | `ROLE_ADMIN` | `hasRole("ADMIN")` | Vai trò tổng quát |
| **Authority/Permission** | `user:read` | `hasAuthority("user:read")` | Quyền cụ thể |

```java
// hasRole("ADMIN") bên trong sẽ check: authority == "ROLE_" + "ADMIN"
// hasAuthority("user:read") check chính xác: authority == "user:read"

// Vì vậy:
hasRole("ADMIN")            ≡  hasAuthority("ROLE_ADMIN")
hasAnyRole("USER", "ADMIN") ≡  hasAnyAuthority("ROLE_USER", "ROLE_ADMIN")
```

### 3.4 UserDetails & UserDetailsService

**UserDetails** = interface đại diện cho user trong Spring Security.
**UserDetailsService** = interface load user từ database.

```
UserDetailsService (interface)
    │
    │  loadUserByUsername(String username)
    │  → throws UsernameNotFoundException
    ▼
UserDetails (interface)
    ├── getUsername()           → String
    ├── getPassword()          → String (đã encode bằng BCrypt)
    ├── getAuthorities()       → Collection<GrantedAuthority>
    ├── isAccountNonExpired()  → boolean (tài khoản hết hạn?)
    ├── isAccountNonLocked()   → boolean (tài khoản bị khóa?)
    ├── isCredentialsNonExpired() → boolean (password hết hạn?)
    └── isEnabled()            → boolean (tài khoản active?)
```

> **Spring Security 6+**: Các method `isAccountNonExpired()`, `isAccountNonLocked()`, 
> `isCredentialsNonExpired()`, `isEnabled()` **default return `true`**.
> Chỉ override khi cần logic custom.

---

## 4. Luồng Xác Thực (Authentication Flow)

### 4.1 Các Thành Phần Tham Gia

```
AuthenticationFilter         Tạo Authentication token từ request
        │
        ▼
AuthenticationManager        Interface điều phối (chỉ 1 method: authenticate())
        │
        ▼
ProviderManager              Implementation mặc định của AuthenticationManager
        │                    Duyệt danh sách AuthenticationProvider
        ▼
AuthenticationProvider       Thực hiện logic xác thực cụ thể
        │                    Mỗi provider hỗ trợ 1 kiểu authentication
        ├── DaoAuthenticationProvider     → Username/Password
        ├── JwtAuthenticationProvider     → JWT Token (Resource Server)
        └── OpaqueTokenAuthProvider       → Opaque Token
        │
        ▼
UserDetailsService           Load user từ DB (cho DaoAuthenticationProvider)
PasswordEncoder              So sánh password (cho DaoAuthenticationProvider)
```

#### Giải thích từng thành phần:

**AuthenticationManager** — Interface chỉ có 1 method:
```java
public interface AuthenticationManager {
    Authentication authenticate(Authentication authentication)
        throws AuthenticationException;
    // Input:  Authentication chưa xác thực (có credentials)
    // Output: Authentication đã xác thực (có authorities)
    // Throw:  Nếu xác thực thất bại
}
```

**ProviderManager** — Implementation mặc định, duyệt qua các provider:
```java
// Pseudo-code bên trong ProviderManager:
for (AuthenticationProvider provider : providers) {
    if (provider.supports(authentication.getClass())) {
        return provider.authenticate(authentication);
        // Tìm được provider hỗ trợ → delegate cho nó
    }
}
// Không provider nào hỗ trợ → throw ProviderNotFoundException
```

**AuthenticationProvider** — Thực hiện xác thực cụ thể:
```java
public interface AuthenticationProvider {
    // Xác thực và trả về Authentication đã xác thực
    Authentication authenticate(Authentication authentication)
        throws AuthenticationException;
    
    // Provider này có hỗ trợ loại Authentication này không?
    boolean supports(Class<?> authentication);
}
```

### 4.2 Luồng Chi Tiết (Username/Password + JWT)

```
1. POST /api/auth/login {username: "admin", password: "123"}
        │
        ▼
2. AuthController gọi:
   authenticationManager.authenticate(
       new UsernamePasswordAuthenticationToken("admin", "123")
   )
        │
        ▼
3. ProviderManager duyệt providers → tìm DaoAuthenticationProvider
        │
        ▼
4. DaoAuthenticationProvider:
   a) Gọi userDetailsService.loadUserByUsername("admin")
      → trả về UserDetails {username, encodedPassword, authorities}
   b) Gọi passwordEncoder.matches("123", encodedPassword)
      → true = đúng password
        │
        ▼
5. Trả về Authentication ĐÃ XÁC THỰC:
   UsernamePasswordAuthenticationToken(userDetails, null, authorities)
        │
        ▼
6. AuthController tạo JWT token từ Authentication
   → Response: { accessToken: "eyJ...", refreshToken: "eyJ..." }

─── Sau đó, các request tiếp theo ───

7. GET /api/users (Header: Authorization: Bearer eyJ...)
        │
        ▼
8. BearerTokenAuthenticationFilter:
   a) Parse header → lấy token string
   b) Gọi JwtDecoder.decode(token) → verify signature + expiry
   c) Gọi JwtAuthenticationConverter → map claims → authorities
   d) Set SecurityContext = JwtAuthenticationToken(jwt, authorities)
        │
        ▼
9. AuthorizationFilter: kiểm tra user có quyền truy cập /api/users?
        │
        ▼
10. Controller xử lý request
```

---

## 5. Cấu Hình SecurityFilterChain — Nền Tảng Cho Cả Phase

> **Code dưới đây là BASE CONFIG** — các file sau sẽ tích hợp thêm vào.

### 5.1 Project Structure

```
src/main/java/com/example/project/
├── config/
│   ├── SecurityConfig.java          ← File này — filter chain
│   ├── JwtConfig.java               ← File 3 (JWT) sẽ tạo
│   └── RsaKeyProperties.java       ← File 3 (JWT) sẽ tạo
│
├── security/
│   ├── CustomUserDetailsService.java   ← File 2 (Auth) sẽ tạo
│   ├── CustomUserDetails.java          ← File 2 (Auth) sẽ tạo
│   ├── JwtService.java                 ← File 3 (JWT) sẽ tạo
│   ├── CustomAuthEntryPoint.java       ← File 2 (Auth) sẽ tạo
│   └── CustomAccessDeniedHandler.java  ← File 2 (Auth) sẽ tạo
│
├── entity/
│   ├── User.java                    ← File 2 (Auth) sẽ tạo
│   ├── Role.java                    ← File 2 (Auth) sẽ tạo
│   └── Permission.java             ← File 2 (Auth) sẽ tạo
│
├── controller/
│   ├── AuthController.java          ← File 3 (JWT) sẽ tạo
│   └── UserController.java
│
└── resources/
    ├── keys/
    │   ├── private.pem              ← File 3 (JWT) sẽ tạo
    │   └── public.pem
    └── application.yml
```

### 5.2 SecurityConfig.java — Base (Sẽ mở rộng ở các file sau)

```java
/**
 * SecurityConfig — Cấu hình bảo mật chính.
 * 
 * ► File 2 (Auth) sẽ thêm: exceptionHandling
 * ► File 3 (JWT) sẽ thêm: oauth2ResourceServer
 * ► File 5 (Best Practice) sẽ thêm: cors config chi tiết
 */
@Configuration
@EnableWebSecurity  // Kích hoạt Spring Security cho web
@EnableMethodSecurity // Kích hoạt @PreAuthorize, @PostAuthorize (File 2 giải thích)
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {

        http
            // ── 1. CSRF ──
            // Disable vì REST API dùng JWT (token ở header, không auto-send)
            // File 5 (Best Practice) giải thích chi tiết khi nào enable/disable
            .csrf(csrf -> csrf.disable())

            // ── 2. Session ──
            // STATELESS = không tạo HttpSession, không lưu SecurityContext vào session
            // Mỗi request phải tự chứng minh (qua JWT token)
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            // ── 3. Authorization Rules ──
            // Thứ tự: specific → general (first match wins)
            .authorizeHttpRequests(auth -> auth
                // Public endpoints — ai cũng truy cập được
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                
                // Admin endpoints — chỉ ROLE_ADMIN
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // Tất cả request khác — phải authenticated
                .anyRequest().authenticated()
            );

            // ── 4. Sẽ thêm ở các file sau ──
            // .oauth2ResourceServer(...)   ← File 3 (JWT)
            // .exceptionHandling(...)      ← File 2 (Auth)
            // .cors(...)                   ← File 5 (Best Practice)

        return http.build();
    }

    /**
     * PasswordEncoder — Mã hóa password.
     * BCrypt(12) = 2^12 = 4096 iterations ≈ 400ms mỗi lần hash.
     * File 5 (Best Practice) giải thích chi tiết.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * AuthenticationManager — Dùng trong AuthController để xác thực login.
     * Spring tự tạo từ UserDetailsService + PasswordEncoder.
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
```

### 5.3 Giải Thích Từng Dòng Config

#### `@EnableWebSecurity`
Kích hoạt Spring Security. Đăng ký `DelegatingFilterProxy` + `FilterChainProxy`.
Không có annotation này → Spring Security không hoạt động.

#### `@EnableMethodSecurity`
Kích hoạt bảo mật ở tầng method (annotation `@PreAuthorize`, `@PostAuthorize`).
Sẽ giải thích chi tiết ở **File 2 — Section 3**.

#### `SessionCreationPolicy.STATELESS`
```
STATELESS:  Mỗi request độc lập, không có session.
            Phù hợp REST API + JWT.
            SecurityContext KHÔNG lưu giữa các request.

IF_REQUIRED: Tạo session khi cần (default).
             Phù hợp server-rendered (Thymeleaf).

ALWAYS:     Luôn tạo session. Ít dùng.
NEVER:      Không tạo, nhưng dùng nếu có sẵn.
```

#### `authorizeHttpRequests()` — Cách Hoạt Động

```
Request: GET /api/admin/users
    │
    ▼
AuthorizationFilter đọc rules theo thứ tự:
    1. /api/auth/**   → KHÔNG match
    2. /api/public/** → KHÔNG match
    3. /api/admin/**  → MATCH → hasRole("ADMIN")
        │
        ├── User có ROLE_ADMIN → ✅ cho qua
        └── User không có      → ❌ AccessDeniedException → 403
```

> **QUAN TRỌNG**: Rules được check theo thứ tự khai báo. Rule **đầu tiên match** sẽ được áp dụng.

```java
// ❌ SAI: .anyRequest().authenticated() ĐẶT TRƯỚC → auth/** cũng bị chặn
.authorizeHttpRequests(auth -> auth
    .anyRequest().authenticated()           // match hết!
    .requestMatchers("/api/auth/**").permitAll() // KHÔNG BAO GIỜ đến đây
);

// ✅ ĐÚNG: Specific trước, general sau
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/auth/**").permitAll() // check trước
    .anyRequest().authenticated()                 // fallback
);
```

---

## 6. Dependency

```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Security core -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- JWT support (File 3 sẽ dùng) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
    </dependency>

    <!-- OAuth2 Client - Social Login (File 4 sẽ dùng) -->
    <!-- <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency> -->
</dependencies>
```

---

## 7. Tóm Tắt: Kiến Trúc Bạn Cần Nhớ

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT REQUEST                        │
│              Authorization: Bearer eyJ...                │
└─────────────────────┬───────────────────────────────────┘
                      │
                      ▼
┌─────────────────────────────────────────────────────────┐
│  DelegatingFilterProxy → FilterChainProxy               │
│              │                                          │
│              ▼                                          │
│  SecurityFilterChain                                    │
│  ┌────────────────────────────────────────────┐         │
│  │ CorsFilter        (File 5)                 │         │
│  │ CsrfFilter        (File 5)                 │         │
│  │ BearerTokenAuthenticationFilter (File 3)   │         │
│  │     │                                      │         │
│  │     ├── JwtDecoder (verify token)          │         │
│  │     ├── JwtAuthConverter (claims→authority)│         │
│  │     └── Set SecurityContext                │         │
│  │ ExceptionTranslationFilter (File 2)        │         │
│  │     │                                      │         │
│  │     ├── AuthenticationEntryPoint → 401     │         │
│  │     └── AccessDeniedHandler → 403          │         │
│  │ AuthorizationFilter                        │         │
│  │     │                                      │         │
│  │     └── Check: hasRole? hasAuthority?      │         │
│  └────────────────────────────────────────────┘         │
│              │                                          │
│              ▼                                          │
│  DispatcherServlet → @RestController                    │
│              │                                          │
│              ▼                                          │
│  @PreAuthorize (File 2) → Service → Repository          │
└─────────────────────────────────────────────────────────┘
```

---

## ✅ Checklist — Sau Khi Đọc File Này

- [ ] Hiểu 3 tầng: DelegatingFilterProxy → FilterChainProxy → SecurityFilterChain
- [ ] Biết SecurityContext lưu ở đâu (ThreadLocal) và chứa gì (Authentication)
- [ ] Biết Authentication có Principal, Credentials, Authorities
- [ ] Phân biệt Role (`ROLE_ADMIN`) vs Authority (`user:read`)
- [ ] Hiểu luồng: AuthenticationManager → ProviderManager → AuthenticationProvider
- [ ] Biết `authorizeHttpRequests()` check theo thứ tự (first match wins)
- [ ] Biết tại sao REST API dùng `STATELESS` + disable CSRF

---

> **Tiếp theo**: Đọc `Phase4_Authentication_vs_Authorization.md` →
> Sẽ tạo `CustomUserDetailsService`, `CustomUserDetails`, Entity RBAC,
> và giải thích chi tiết `@PreAuthorize` / `@PostAuthorize`.
