# Bài 12: Bảo Mật Trong Spring Boot (Spring Security)

> **Stack**: Spring Boot **3.4.3** · Spring Security **6.4.x** · JJWT **0.12.6** · Java **17+**

## Mục Tiêu

Sau khi hoàn thành bài này, bạn sẽ nắm được:

- Cơ chế **Authentication** (Xác thực) và **Authorization** (Phân quyền) trong Spring Security
- Cách triển khai **JWT (JSON Web Token)** cho REST API
- Tích hợp **OAuth2** với các nhà cung cấp bên ngoài (Google, GitHub)
- Các **Best Practice** bảo mật chuẩn production

---

## Nội Dung

| File | Nội dung |
|------|----------|
| [01_Authentication_Authorization.md](01_Authentication_Authorization.md) | Xác thực & Phân quyền với Spring Security |
| [02_JWT.md](02_JWT.md) | JSON Web Token — Lý thuyết & Triển khai |
| [03_OAuth2.md](03_OAuth2.md) | OAuth2 — Đăng nhập qua Google / GitHub |
| [04_Best_Practices.md](04_Best_Practices.md) | Best Practices: BCrypt, CORS, CSRF, ProblemDetail |

---

## Tổng Quan Spring Security

```
HTTP Request
    │
    ▼
┌─────────────────────────────────────┐
│         Filter Chain                │
│  ┌───────────────────────────────┐  │
│  │  UsernamePasswordAuthFilter   │  │
│  │  JwtAuthenticationFilter      │  │
│  │  OAuth2LoginAuthFilter        │  │
│  └───────────────────────────────┘  │
└────────────────┬────────────────────┘
                 │
                 ▼
    AuthenticationManager
                 │
                 ▼
    AuthenticationProvider
                 │
                 ▼
        UserDetailsService
                 │
                 ▼
         SecurityContext
                 │
                 ▼
    Authorization (Role/Permission)
                 │
                 ▼
         Controller / Resource
```

---

## Yêu Cầu Trước Khi Học

- Đã hoàn thành Phase 1, 2, 3 trong RoadMap
- Hiểu về HTTP, REST API, Spring IoC/DI
- JDK 17 trở lên

---

## Dependencies (pom.xml)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.4.3</version>   <!-- Spring Boot mới nhất (03/2026) -->
</parent>

<dependencies>
    <!-- Spring Security -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>

    <!-- Bean Validation (@Valid, @NotBlank...) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- JJWT 0.12.6 — API mới nhất, không dùng parserBuilder() nữa -->
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

    <!-- OAuth2 Client (tuỳ chọn) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-oauth2-client</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <optional>true</optional>
    </dependency>
</dependencies>
```

---

## Thay Đổi Lớn Từ Spring Security 5 → 6

| Cũ (Spring Security 5) | Mới (Spring Security 6.4.x) |
|------------------------|-----------------------------|
| `WebSecurityConfigurerAdapter` | **Xóa hoàn toàn** — dùng Bean `SecurityFilterChain` |
| `antMatchers()` | `requestMatchers()` |
| `authorizeRequests()` | `authorizeHttpRequests()` |
| `.and()` chaining | Lambda DSL — không có `and()` |
| `cors()` auto-configured | Phải khai báo `CorsConfigurationSource` bean |
| `UserDetailsService.loadUserByUsername()` throws checked ex | Giữ nguyên |
| `Jwts.parserBuilder()` (JJWT < 0.12) | `Jwts.parser()` (JJWT ≥ 0.12) |

> ⚠️ **Senior note**: Spring Security 6 bắt buộc dùng **Lambda DSL** cho tất cả cấu hình `HttpSecurity`. Code kiểu cũ `http.cors().and().csrf().disable()` sẽ **không biên dịch được**.

---

## Cấu Trúc Package Khuyên Dùng

```
src/main/java/com/yourapp/
├── config/
│   ├── SecurityConfig.java          ← SecurityFilterChain, beans bảo mật
│   ├── CorsConfig.java              ← CorsConfigurationSource
│   └── JwtProperties.java           ← @ConfigurationProperties
├── security/
│   ├── jwt/
│   │   ├── JwtService.java          ← tạo / parse / validate token
│   │   └── JwtAuthenticationFilter.java
│   ├── oauth2/
│   │   ├── OAuth2UserService.java
│   │   ├── OAuth2SuccessHandler.java
│   │   └── userinfo/                ← GoogleUserInfo, GithubUserInfo ...
│   ├── UserPrincipal.java           ← implements UserDetails
│   ├── UserDetailsServiceImpl.java
│   └── JwtAuthEntryPoint.java
├── auth/
│   ├── AuthController.java
│   ├── dto/
│   │   ├── LoginRequest.java        ← Java record
│   │   ├── RegisterRequest.java     ← Java record
│   │   └── AuthResponse.java        ← Java record
│   └── AuthService.java
└── exception/
    └── GlobalExceptionHandler.java  ← @RestControllerAdvice + ProblemDetail
```
