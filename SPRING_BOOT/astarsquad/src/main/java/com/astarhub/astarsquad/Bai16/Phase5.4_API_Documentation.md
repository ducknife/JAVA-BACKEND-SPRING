# 📖 Phase 5.4: API Documentation — Swagger/OpenAPI + SpringDoc

---

## 📑 Mục Lục

- [1. API Documentation Là Gì?](#1-api-documentation-là-gì)
- [2. OpenAPI vs Swagger](#2-openapi-vs-swagger)
- [3. Setup SpringDoc](#3-setup-springdoc)
- [4. Cấu Hình Chi Tiết](#4-cấu-hình-chi-tiết)
- [5. Annotations Mô Tả API](#5-annotations-mô-tả-api)
- [6. Tích Hợp Security (JWT)](#6-tích-hợp-security-jwt)
- [7. Grouping & Multiple APIs](#7-grouping--multiple-apis)
- [✅ Checklist](#-checklist)

---

## 1. API Documentation Là Gì?

```
Không có docs:
Frontend dev: "API endpoint nào? Params gì? Response format?"
Backend dev:  "Để tôi giải thích..." (mỗi lần mỗi giải thích)

Có docs (auto-generated):
Frontend dev: Mở http://localhost:8080/swagger-ui → Đọc + Test luôn
              → Biết endpoints, params, response, errors
              → Không cần hỏi backend
```

---

## 2. OpenAPI vs Swagger

| | OpenAPI | Swagger |
|---|--------|---------|
| **Là gì** | Specification (chuẩn mô tả API) | Toolset (UI, Codegen, Editor) |
| **Version** | OpenAPI 3.0/3.1 | Swagger UI (hiển thị docs) |
| **Trong Spring** | SpringDoc tạo OpenAPI spec | Swagger UI render spec thành web |

---

## 3. Setup SpringDoc

```xml
<!-- pom.xml — CHỈ CẦN 1 dependency -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
</dependency>
```

```yaml
# application.yml
springdoc:
  api-docs:
    path: /v3/api-docs          # JSON spec endpoint
  swagger-ui:
    path: /swagger-ui.html      # Swagger UI URL
    operations-sorter: method    # Sắp xếp theo HTTP method
    tags-sorter: alpha           # Sắp xếp tags A-Z
```

**Truy cập:**
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

---

## 4. Cấu Hình Chi Tiết

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("My API")
                .version("1.0.0")
                .description("REST API documentation")
                .contact(new Contact()
                    .name("Dev Team")
                    .email("dev@example.com"))
                .license(new License()
                    .name("MIT")))
            .externalDocs(new ExternalDocumentation()
                .description("GitHub Repository")
                .url("https://github.com/your/repo"));
    }
}
```

---

## 5. Annotations Mô Tả API

```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management APIs")
public class UserController {

    @Operation(
        summary = "Get user by ID",
        description = "Returns a single user by their unique ID"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "User found",
            content = @Content(schema = @Schema(implementation = UserResponse.class))),
        @ApiResponse(responseCode = "404", description = "User not found"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUser(
            @Parameter(description = "User ID", example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(userService.findById(id));
    }

    @Operation(summary = "Create new user")
    @ApiResponse(responseCode = "201", description = "User created")
    @PostMapping
    public ResponseEntity<UserResponse> createUser(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                description = "User data",
                required = true,
                content = @Content(schema = @Schema(implementation = CreateUserRequest.class)))
            @Valid @RequestBody CreateUserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.createUser(request));
    }
}
```

**Schema trên DTO:**

```java
@Schema(description = "User creation request")
public record CreateUserRequest(
    @Schema(description = "Username", example = "john_doe",
            minLength = 3, maxLength = 50)
    @NotBlank String username,

    @Schema(description = "Email address", example = "john@mail.com")
    @Email String email,

    @Schema(description = "Password", example = "Pass123!",
            minLength = 8)
    @NotBlank String password
) {}
```

---

## 6. Tích Hợp Security (JWT)

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info().title("My API").version("1.0.0"))
            // Thêm JWT security scheme
            .addSecurityItem(new SecurityRequirement()
                .addList("Bearer Authentication"))
            .components(new Components()
                .addSecuritySchemes("Bearer Authentication",
                    new SecurityScheme()
                        .name("Bearer Authentication")
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .description("Enter JWT token")));
    }
}
```

```yaml
# Cho phép Swagger UI bypass security
# SecurityConfig — thêm vào permitAll:
.requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
```

---

## 7. Grouping & Multiple APIs

```java
@Bean
public GroupedOpenApi publicApi() {
    return GroupedOpenApi.builder()
        .group("public")
        .pathsToMatch("/api/public/**", "/api/auth/**")
        .build();
}

@Bean
public GroupedOpenApi adminApi() {
    return GroupedOpenApi.builder()
        .group("admin")
        .pathsToMatch("/api/admin/**")
        .build();
}
```

---

## ✅ Checklist

- [ ] Thêm dependency springdoc-openapi
- [ ] Truy cập Swagger UI tại /swagger-ui.html
- [ ] Dùng @Tag, @Operation, @ApiResponse trên Controller
- [ ] Dùng @Schema trên DTO
- [ ] Tích hợp JWT authentication trong Swagger
- [ ] Permit Swagger endpoints trong SecurityConfig

---

> **Tiếp theo**: Đọc `Phase5.5_Caching.md` →
