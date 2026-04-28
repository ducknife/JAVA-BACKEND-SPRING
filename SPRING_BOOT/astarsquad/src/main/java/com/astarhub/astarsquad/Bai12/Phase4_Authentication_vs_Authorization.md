# 🔐 Authentication vs Authorization — Chi Tiết

> **Tích hợp từ**: File 1 (Overview) — SecurityConfig base
> **File này tạo**: CustomUserDetailsService, CustomUserDetails, Entity RBAC, Exception Handlers
> **Code ở đây sẽ được dùng lại** ở File 3 (JWT) và File 4 (OAuth2)

---

## 📑 Mục Lục

- [1. Authentication vs Authorization — Phân Biệt](#1-authentication-vs-authorization--phân-biệt)
- [2. Authentication — Triển Khai Code](#2-authentication--triển-khai-code)
  - [2.1 Entity Layer — User, Role, Permission (RBAC)](#21-entity-layer--user-role-permission-rbac)
  - [2.2 CustomUserDetails](#22-customuserdetails--adapter-user--spring-security)
  - [2.3 CustomUserDetailsService](#23-customuserdetailsservice--load-user-từ-db)
  - [2.4 Exception Handlers — 401 và 403](#24-exception-handlers--401-và-403)
  - [2.5 Tích Hợp Vào SecurityConfig](#25-tích-hợp-vào-securityconfig-cập-nhật-file-1)
- [3. Authorization — @PreAuthorize & @PostAuthorize](#3-authorization--preauthorize--postauthorize-chi-tiết)
  - [3.1 @PreAuthorize](#31-preauthorize--kiểm-tra-trước-khi-method-chạy)
  - [3.2 @PostAuthorize](#32-postauthorize--kiểm-tra-sau-khi-method-chạy)
  - [3.3 @PreFilter & @PostFilter](#33-prefilter--postfilter--lọc-collection)
  - [3.4 Custom Permission Evaluator](#34-custom-permission-evaluator--logic-phân-quyền-phức-tạp)
  - [3.5 Custom Permission Service](#35-custom-permission-service--cách-đơn-giản-hơn)
- [4. Lấy User Hiện Tại — 3 Cách](#4-lấy-user-hiện-tại--3-cách)
- [5. HTTP Authorization vs Method Authorization](#5-http-authorization-vs-method-authorization)
- [✅ Checklist](#-checklist--sau-khi-đọc-file-này)

---

## 1. Authentication vs Authorization — Phân Biệt

| | Authentication (Xác thực) | Authorization (Phân quyền) |
|---|---|---|
| **Câu hỏi** | "Bạn là ai?" | "Bạn được làm gì?" |
| **Thời điểm** | Xảy ra **TRƯỚC** | Xảy ra **SAU** authentication |
| **Filter** | `BearerTokenAuthenticationFilter` (#8) | `AuthorizationFilter` (#14) |
| **Input** | Credentials (password, JWT token) | User đã xác thực + resource yêu cầu |
| **Output** | `Authentication` object (có authorities) | Allow / Deny (`AccessDeniedException`) |
| **Interface** | `AuthenticationManager` | `AuthorizationManager` |
| **Exception** | `AuthenticationException` → 401 | `AccessDeniedException` → 403 |

```
Request: GET /api/admin/users (Authorization: Bearer eyJ...)
    │
    ▼
[Authentication Phase]
BearerTokenAuthenticationFilter
    ├── Parse JWT token
    ├── JwtDecoder verify signature + expiry
    ├── JwtAuthConverter: claims → authorities
    └── SecurityContext = {user="admin", authorities=[ROLE_ADMIN]}
    │
    ▼
[Authorization Phase]
AuthorizationFilter
    ├── Rule: /api/admin/** → hasRole("ADMIN")
    ├── User authorities chứa ROLE_ADMIN?
    ├── ✅ CÓ → cho qua → Controller
    └── ❌ KHÔNG → AccessDeniedException → 403
```

---

## 2. Authentication — Triển Khai Code

### 2.1 Entity Layer — User, Role, Permission (RBAC)

> **RBAC** = Role-Based Access Control.
> User → có nhiều Role → mỗi Role có nhiều Permission.

```java
// ===== User.java =====
// → Tích hợp vào: entity/ package (xem project structure ở File 1)

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;  // Luôn lưu đã encode (BCrypt)

    @Column(unique = true)
    private String email;

    private boolean active = true;    // Tài khoản có hoạt động?
    private boolean locked = false;   // Tài khoản bị khóa?

    /**
     * EAGER fetch: load roles ngay khi load user.
     * Cần thiết vì UserDetailsService cần authorities ngay lập tức.
     * 
     * Bảng trung gian user_roles:
     * | user_id | role_id |
     * |---------|---------|
     * | 1       | 1       |  ← user 1 có role 1 (ADMIN)
     * | 1       | 2       |  ← user 1 có role 2 (USER)
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
}
```

```java
// ===== Role.java =====

@Entity
@Table(name = "roles")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;  // ADMIN, USER, MODERATOR (KHÔNG có prefix ROLE_)

    /**
     * Role → nhiều Permission.
     * Ví dụ: ADMIN → [user:read, user:write, user:delete, post:write]
     *         USER  → [user:read, post:write]
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();
}
```

```java
// ===== Permission.java =====

@Entity
@Table(name = "permissions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Permission {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;  // user:read, user:write, post:delete
                          // Dùng format "resource:action"
}
```

### 2.2 CustomUserDetails — Adapter User → Spring Security

> **Tại sao cần?** Spring Security chỉ hiểu `UserDetails` interface.
> Entity `User` của bạn phải được "dịch" sang `UserDetails`.

```java
// ===== security/CustomUserDetails.java =====
// → Adapter Pattern: wrap User entity thành UserDetails

public class CustomUserDetails implements UserDetails {

    private final User user;

    public CustomUserDetails(User user) {
        this.user = user;
    }

    /**
     * Chuyển đổi Roles + Permissions → flat list GrantedAuthority.
     * 
     * Ví dụ User có Role ADMIN với permissions [user:read, user:write]:
     * → authorities = [ROLE_ADMIN, user:read, user:write]
     *                   ↑ role       ↑ permissions
     * 
     * hasRole("ADMIN")         → check "ROLE_ADMIN" ✅
     * hasAuthority("user:read") → check "user:read" ✅
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();

        for (Role role : user.getRoles()) {
            // Thêm role với prefix "ROLE_"
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // Thêm tất cả permissions của role (không prefix)
            for (Permission permission : role.getPermissions()) {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            }
        }

        return authorities;
    }

    @Override
    public String getPassword() { return user.getPassword(); }

    @Override
    public String getUsername() { return user.getUsername(); }

    // Spring Security 6+: default return true
    // Override khi cần logic custom
    @Override
    public boolean isAccountNonLocked() { return !user.isLocked(); }

    @Override
    public boolean isEnabled() { return user.isActive(); }

    // ===== Custom methods — truy cập thêm thông tin =====
    public Long getUserId() { return user.getId(); }
    public String getEmail() { return user.getEmail(); }
    public User getUser() { return user; }
}
```

### 2.3 CustomUserDetailsService — Load User Từ DB

```java
// ===== security/CustomUserDetailsService.java =====
// → Được DaoAuthenticationProvider gọi khi xác thực

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security gọi method này khi cần xác thực user.
     * 
     * Luồng:
     * AuthenticationManager.authenticate()
     *   → ProviderManager
     *     → DaoAuthenticationProvider
     *       → userDetailsService.loadUserByUsername() ← ĐANG Ở ĐÂY
     *         → return UserDetails
     *       → passwordEncoder.matches() ← so sánh password
     *     → return Authentication (đã xác thực)
     */
    @Override
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException(
                "User not found with username: " + username
            ));

        // Wrap entity User → CustomUserDetails (UserDetails)
        return new CustomUserDetails(user);
    }
}
```

### 2.4 Exception Handlers — 401 và 403

> **Tích hợp vào**: SecurityConfig (File 1) → `.exceptionHandling(...)`

```java
// ===== security/CustomAuthEntryPoint.java =====
// Xử lý khi user CHƯA XÁC THỰC truy cập endpoint protected → 401

@Component
@RequiredArgsConstructor
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    /**
     * Được gọi bởi ExceptionTranslationFilter khi:
     * - Không có token trong request
     * - Token hết hạn hoặc invalid
     * - Bất kỳ AuthenticationException nào
     */
    @Override
    public void commence(HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401

        Map<String, Object> body = Map.of(
            "status", 401,
            "error", "Unauthorized",
            "message", "Bạn cần đăng nhập để truy cập resource này",
            "path", request.getRequestURI(),
            "timestamp", LocalDateTime.now().toString()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
```

```java
// ===== security/CustomAccessDeniedHandler.java =====
// Xử lý khi user ĐÃ XÁC THỰC nhưng KHÔNG ĐỦ QUYỀN → 403

@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    /**
     * Được gọi bởi ExceptionTranslationFilter khi:
     * - User đã authenticate nhưng không có role/authority cần thiết
     * - @PreAuthorize check thất bại
     */
    @Override
    public void handle(HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403

        Map<String, Object> body = Map.of(
            "status", 403,
            "error", "Forbidden",
            "message", "Bạn không có quyền truy cập resource này",
            "path", request.getRequestURI(),
            "timestamp", LocalDateTime.now().toString()
        );
        objectMapper.writeValue(response.getOutputStream(), body);
    }
}
```

### 2.5 Tích Hợp Vào SecurityConfig (Cập nhật File 1)

```java
// ===== Thêm vào SecurityConfig.java (File 1) =====

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    // ★ THÊM MỚI — inject exception handlers
    private final CustomAuthEntryPoint authEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http)
            throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/api/public/**").permitAll()
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            )

            // ★ THÊM MỚI — Exception handling
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(authEntryPoint)   // 401
                .accessDeniedHandler(accessDeniedHandler)   // 403
            );

            // File 3 (JWT) sẽ thêm: .oauth2ResourceServer(...)

        return http.build();
    }

    // ... passwordEncoder(), authenticationManager() giữ nguyên
}
```

---

## 3. Authorization — @PreAuthorize & @PostAuthorize Chi Tiết

### 3.1 @PreAuthorize — Kiểm Tra TRƯỚC Khi Method Chạy

> **Pre** = trước. Check điều kiện **trước khi** method thực thi.
> Nếu điều kiện sai → ném `AccessDeniedException` → method **KHÔNG chạy**.

```
@PreAuthorize("hasRole('ADMIN')")
public void deleteUser(Long id) { ... }

Request đến:
    │
    ▼
Spring AOP Proxy intercept method call
    │
    ▼
Đọc @PreAuthorize expression: "hasRole('ADMIN')"
    │
    ▼
Lấy Authentication từ SecurityContext
    │
    ▼
Evaluate SpEL: authentication.authorities chứa ROLE_ADMIN?
    │
    ├── ✅ CÓ → Gọi method deleteUser() → trả kết quả
    └── ❌ KHÔNG → throw AccessDeniedException → 403 (method KHÔNG chạy)
```

**Các expression phổ biến:**

```java
@Service
public class UserService {

    // ── Check Role ──
    @PreAuthorize("hasRole('ADMIN')")
    // Bên trong: check authorities chứa "ROLE_ADMIN"
    public void deleteUser(Long id) { ... }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    // Check: có ROLE_ADMIN HOẶC ROLE_MODERATOR
    public void banUser(Long id) { ... }

    // ── Check Authority (Permission) ──
    @PreAuthorize("hasAuthority('user:write')")
    // Check chính xác: authorities chứa "user:write"
    public void updateUser(Long id, UserDTO dto) { ... }

    // ── Check Authentication ──
    @PreAuthorize("isAuthenticated()")
    // Chỉ cần đã đăng nhập, không cần role cụ thể
    public UserDTO getProfile() { ... }

    // ── SpEL với tham số method ──
    @PreAuthorize("#userId == authentication.principal.userId")
    // #userId = tham số method, so sánh với userId trong JWT
    // → User chỉ sửa được chính mình
    public void updateProfile(Long userId, ProfileDTO dto) { ... }

    // ── Kết hợp nhiều điều kiện ──
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    // Admin sửa được tất cả, user thường chỉ sửa chính mình
    public void updateUser(Long userId, UserDTO dto) { ... }

    // ── Gọi bean method ──
    @PreAuthorize("@permissionService.canEdit(#postId, authentication)")
    // Delegate cho custom service kiểm tra logic phức tạp
    public void editPost(Long postId, PostDTO dto) { ... }
}
```

### 3.2 @PostAuthorize — Kiểm Tra SAU Khi Method Chạy

> **Post** = sau. Method **chạy xong**, kiểm tra kết quả trả về.
> Nếu điều kiện sai → ném `AccessDeniedException` → client nhận 403.
> **Lưu ý**: Method ĐÃ CHẠY (side effects có thể đã xảy ra).

```
@PostAuthorize("returnObject.authorId == authentication.principal.userId")
public Post getPost(Long id) { return postRepository.findById(id); }

Request đến:
    │
    ▼
Method getPost() CHẠY → trả về Post object
    │
    ▼
Đọc @PostAuthorize expression
    │
    ▼
Evaluate SpEL: returnObject.authorId == authentication.principal.userId?
    │
    ├── ✅ ĐÚNG → trả Post cho client
    └── ❌ SAI → throw AccessDeniedException → 403
                  (nhưng query DB ĐÃ CHẠY rồi!)
```

**Khi nào dùng @PostAuthorize?**

```java
@Service
public class PostService {

    // Chỉ cho user xem post của chính mình (draft)
    @PostAuthorize("returnObject.authorId == authentication.principal.userId "
                  + "or returnObject.status == 'PUBLISHED'")
    public Post getPost(Long id) {
        // Method chạy trước, rồi mới check
        return postRepository.findById(id)
            .orElseThrow(() -> new NotFoundException("Post not found"));
    }
    // → Nếu post là draft của người khác → 403

    // Return null thì sao? Cần handle:
    @PostAuthorize("returnObject == null or returnObject.ownerId == authentication.principal.userId")
    public Order getOrder(Long id) { ... }
}
```

### 3.3 @PreFilter & @PostFilter — Lọc Collection

```java
@Service
public class PostService {

    /**
     * @PreFilter: LỌC input collection TRƯỚC khi method chạy.
     * filterObject = từng phần tử trong collection.
     * 
     * Ví dụ: User gửi list [post1, post2, post3]
     * Chỉ giữ lại post mà user là author → [post1, post3]
     * Method nhận được list đã lọc.
     */
    @PreFilter("filterObject.authorId == authentication.principal.userId")
    public void deletePosts(List<Post> posts) {
        // posts đã được lọc — chỉ chứa post của user hiện tại
        postRepository.deleteAll(posts);
    }

    /**
     * @PostFilter: LỌC output collection SAU khi method chạy.
     * 
     * Method trả về tất cả posts, nhưng user chỉ nhận được
     * posts mà họ là author.
     */
    @PostFilter("filterObject.authorId == authentication.principal.userId "
              + "or filterObject.status == 'PUBLISHED'")
    public List<Post> getAllPosts() {
        return postRepository.findAll();
        // Kết quả bị lọc trước khi trả cho caller
    }
}
```

### 3.4 Custom Permission Evaluator — Logic Phân Quyền Phức Tạp

> Khi `@PreAuthorize` expression quá phức tạp → tách ra **PermissionEvaluator**.

```java
// ===== security/CustomPermissionEvaluator.java =====

@Component
@RequiredArgsConstructor
public class CustomPermissionEvaluator implements PermissionEvaluator {

    private final PostRepository postRepository;

    /**
     * Được gọi khi dùng: @PreAuthorize("hasPermission(#object, 'permission')")
     *
     * @param auth          Authentication hiện tại
     * @param targetObject  Object cần kiểm tra quyền
     * @param permission    Quyền cần kiểm tra (String)
     */
    @Override
    public boolean hasPermission(
            Authentication auth, Object targetObject, Object permission) {

        if (targetObject instanceof Post post) {
            CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
            String perm = (String) permission;

            return switch (perm) {
                case "edit" ->
                    // Author được edit, hoặc ADMIN
                    post.getAuthorId().equals(user.getUserId())
                    || hasRole(auth, "ADMIN");
                case "delete" ->
                    // Chỉ ADMIN được xóa
                    hasRole(auth, "ADMIN");
                case "view" ->
                    // Published → ai cũng xem
                    // Draft → chỉ author hoặc ADMIN
                    "PUBLISHED".equals(post.getStatus())
                    || post.getAuthorId().equals(user.getUserId())
                    || hasRole(auth, "ADMIN");
                default -> false;
            };
        }
        return false;
    }

    /**
     * Được gọi khi dùng: @PreAuthorize("hasPermission(#id, 'Post', 'edit')")
     * Load object bằng ID rồi kiểm tra.
     */
    @Override
    public boolean hasPermission(
            Authentication auth, Serializable targetId,
            String targetType, Object permission) {

        if ("Post".equals(targetType) && targetId instanceof Long id) {
            Post post = postRepository.findById(id).orElse(null);
            if (post == null) return false;
            return hasPermission(auth, post, permission);
        }
        return false;
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
```

```java
// Sử dụng trong Service:
@Service
public class PostService {

    @PreAuthorize("hasPermission(#postId, 'Post', 'edit')")
    public Post updatePost(Long postId, PostUpdateDTO dto) {
        Post post = postRepository.findById(postId).orElseThrow();
        post.setTitle(dto.getTitle());
        return postRepository.save(post);
    }

    @PreAuthorize("hasPermission(#post, 'delete')")
    public void deletePost(Post post) {
        postRepository.delete(post);
    }
}
```

### 3.5 Custom Permission Service — Cách Đơn Giản Hơn

> Không cần PermissionEvaluator, dùng trực tiếp SpEL gọi bean:

```java
@Service("perm")  // tên bean ngắn gọn cho SpEL
@RequiredArgsConstructor
public class PermissionService {

    private final PostRepository postRepository;

    public boolean canEditPost(Long postId, Authentication auth) {
        Post post = postRepository.findById(postId).orElse(null);
        if (post == null) return false;

        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return post.getAuthorId().equals(user.getUserId())
            || auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }

    public boolean isOwner(Long resourceOwnerId, Authentication auth) {
        CustomUserDetails user = (CustomUserDetails) auth.getPrincipal();
        return resourceOwnerId.equals(user.getUserId());
    }
}
```

```java
// Sử dụng — gọi bean bằng @beanName trong SpEL:
@PreAuthorize("@perm.canEditPost(#postId, authentication)")
public Post updatePost(Long postId, PostUpdateDTO dto) { ... }

@PreAuthorize("@perm.isOwner(#userId, authentication) or hasRole('ADMIN')")
public void deleteAccount(Long userId) { ... }
```

---

## 4. Lấy User Hiện Tại — 3 Cách

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    // ===== Cách 1: @AuthenticationPrincipal =====
    // Inject principal trực tiếp vào parameter
    // Nếu dùng JWT Resource Server → principal là Jwt
    // Nếu dùng UserDetailsService → principal là UserDetails
    @GetMapping("/me")
    public ResponseEntity<?> me(
            @AuthenticationPrincipal Jwt jwt) {  // hoặc CustomUserDetails
        return ResponseEntity.ok(Map.of(
            "username", jwt.getSubject(),
            "userId", jwt.getClaimAsString("userId")
        ));
    }

    // ===== Cách 2: SecurityContextHolder (dùng ở Service) =====
    @GetMapping("/me2")
    public ResponseEntity<?> me2() {
        Authentication auth = SecurityContextHolder.getContext()
            .getAuthentication();
        // auth.getName() = username
        // auth.getAuthorities() = quyền
        return ResponseEntity.ok(auth.getName());
    }

    // ===== Cách 3: JwtAuthenticationToken =====
    @GetMapping("/me3")
    public ResponseEntity<?> me3(JwtAuthenticationToken authToken) {
        Jwt jwt = authToken.getToken();
        Collection<GrantedAuthority> authorities = authToken.getAuthorities();
        return ResponseEntity.ok(Map.of(
            "user", jwt.getSubject(),
            "authorities", authorities.stream()
                .map(GrantedAuthority::getAuthority).toList()
        ));
    }
}
```

---

## 5. HTTP Authorization vs Method Authorization

| | HTTP Authorization | Method Authorization |
|---|---|---|
| **Ở đâu** | `SecurityConfig` → `authorizeHttpRequests()` | `@PreAuthorize` trên method |
| **Dựa trên** | URL pattern + HTTP method | Business logic + parameters |
| **Granularity** | Endpoint level | Method/field level |
| **Khi nào dùng** | Phân quyền thô (public vs protected) | Phân quyền tinh (owner check) |

```java
// HTTP Authorization — SecurityConfig
.authorizeHttpRequests(auth -> auth
    .requestMatchers("/api/admin/**").hasRole("ADMIN")  // thô
    .anyRequest().authenticated()
)

// Method Authorization — Service layer
@PreAuthorize("#userId == authentication.principal.userId or hasRole('ADMIN')")
public void updateProfile(Long userId, ProfileDTO dto) { ... }  // tinh
```

> **Best Practice**: Dùng CẢ HAI. HTTP rules cho phân quyền thô,
> Method security cho phân quyền tinh theo business logic.

---

## ✅ Checklist — Sau Khi Đọc File Này

- [ ] Tạo được Entity: User → Role → Permission (RBAC)
- [ ] Implement CustomUserDetails (adapter pattern)
- [ ] Implement CustomUserDetailsService (load từ DB)
- [ ] Tạo CustomAuthEntryPoint (401) + CustomAccessDeniedHandler (403)
- [ ] Tích hợp exception handling vào SecurityConfig
- [ ] Hiểu @PreAuthorize chạy TRƯỚC method (AOP proxy)
- [ ] Hiểu @PostAuthorize chạy SAU method (check returnObject)
- [ ] Hiểu @PreFilter/@PostFilter lọc collection
- [ ] Biết dùng SpEL gọi custom bean: `@perm.canEdit(...)`
- [ ] Phân biệt HTTP Authorization vs Method Authorization

---

> **Tiếp theo**: Đọc `Phase4_JWT_Token.md` →
> Sẽ tạo JwtConfig, JwtService, AuthController — tích hợp code từ file này.
