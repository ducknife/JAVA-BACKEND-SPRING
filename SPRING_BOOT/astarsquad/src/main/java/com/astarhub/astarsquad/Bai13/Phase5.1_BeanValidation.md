# ✅ Phase 5.1: Bean Validation

> **Ref**: [Jakarta Bean Validation](https://beanvalidation.org/) + [Spring Validation](https://docs.spring.io/spring-framework/reference/core/validation/beanvalidation.html)
> **Dependency**: `spring-boot-starter-validation` (Spring Boot 3.x tự bao gồm)

---

## 📑 Mục Lục

- [1. Bean Validation Là Gì?](#1-bean-validation-là-gì)
- [2. Các Annotation Chuẩn](#2-các-annotation-chuẩn)
- [3. Sử Dụng @Valid Trong Controller](#3-sử-dụng-valid-trong-controller)
- [4. Validation Groups](#4-validation-groups)
- [5. Custom Validator](#5-custom-validator)
- [6. Xử Lý Lỗi Validation — Tích Hợp @ControllerAdvice](#6-xử-lý-lỗi-validation--tích-hợp-controlleradvice)
- [7. Validate Nested Objects & Collections](#7-validate-nested-objects--collections)
- [8. Validate Path Variables & Request Params](#8-validate-path-variables--request-params)
- [9. Validate Ở Service Layer](#9-validate-ở-service-layer)
- [✅ Checklist](#-checklist)

---

## 1. Bean Validation Là Gì?

**Bean Validation** = chuẩn Java (JSR 380) cho phép kiểm tra dữ liệu bằng **annotation** trực tiếp trên field.

```
Không có Validation:
Controller nhận data → Service xử lý → DB lưu → Lỗi runtime!
                                                  ↑ Data sai nhưng vẫn lọt

Có Validation:
Controller nhận data → @Valid check → ❌ Sai → 400 Bad Request (ngay lập tức)
                                    → ✅ Đúng → Service xử lý → DB lưu
```

**Luồng hoạt động:**

```
Client gửi: POST /api/users { "username": "", "email": "abc" }
    │
    ▼
@Valid trên @RequestBody
    │
    ▼
Hibernate Validator đọc annotations trên DTO
    ├── @NotBlank username → "" → ❌ FAIL
    ├── @Email email → "abc" → ❌ FAIL
    └── Tổng hợp lỗi → MethodArgumentNotValidException
    │
    ▼
@ControllerAdvice bắt Exception → Response 400:
{
  "errors": {
    "username": "must not be blank",
    "email": "must be a well-formed email address"
  }
}
```

---

## 2. Các Annotation Chuẩn

### 2.1 Kiểm Tra Null/Empty

| Annotation | Áp dụng cho | Điều kiện PASS |
|-----------|------------|---------------|
| `@NotNull` | Mọi type | `value != null` (cho phép "") |
| `@NotEmpty` | String, Collection, Map, Array | `!= null && size > 0` |
| `@NotBlank` | String | `!= null && trim().length() > 0` |
| `@Null` | Mọi type | `value == null` |

```java
@NotNull    // "hi" ✅ | "" ✅ | "  " ✅ | null ❌
@NotEmpty   // "hi" ✅ | "" ❌ | "  " ✅ | null ❌
@NotBlank   // "hi" ✅ | "" ❌ | "  " ❌ | null ❌
```

### 2.2 Kiểm Tra Kích Thước

| Annotation | Dùng cho | Ví dụ |
|-----------|---------|-------|
| `@Size(min, max)` | String, Collection | `@Size(min=2, max=50)` |
| `@Min(value)` | Number | `@Min(0)` — không âm |
| `@Max(value)` | Number | `@Max(100)` |
| `@DecimalMin` | BigDecimal, String | `@DecimalMin("0.01")` |
| `@DecimalMax` | BigDecimal, String | `@DecimalMax("999.99")` |
| `@Digits(integer, fraction)` | Number | `@Digits(integer=5, fraction=2)` |
| `@Positive` | Number | `> 0` |
| `@PositiveOrZero` | Number | `>= 0` |
| `@Negative` | Number | `< 0` |

### 2.3 Kiểm Tra Format

| Annotation | Ví dụ |
|-----------|-------|
| `@Email` | `@Email` — phải là email hợp lệ |
| `@Pattern(regexp)` | `@Pattern(regexp = "^[A-Za-z0-9]+$")` |
| `@Past` | Date phải ở quá khứ |
| `@Future` | Date phải ở tương lai |
| `@PastOrPresent` | Date ≤ hiện tại |
| `@FutureOrPresent` | Date ≥ hiện tại |

### 2.4 Ví Dụ DTO Hoàn Chỉnh

```java
public class CreateUserRequest {

    @NotBlank(message = "Username không được để trống")
    @Size(min = 3, max = 50, message = "Username phải từ 3-50 ký tự")
    @Pattern(regexp = "^[a-zA-Z0-9_]+$",
             message = "Username chỉ chứa chữ, số và dấu gạch dưới")
    private String username;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không hợp lệ")
    private String email;

    @NotBlank(message = "Password không được để trống")
    @Size(min = 8, message = "Password phải ít nhất 8 ký tự")
    private String password;

    @Min(value = 0, message = "Tuổi không được âm")
    @Max(value = 150, message = "Tuổi không hợp lệ")
    private Integer age;

    @PastOrPresent(message = "Ngày sinh phải ở quá khứ")
    private LocalDate birthDate;
}
```

---

## 3. Sử Dụng @Valid Trong Controller

```java
@RestController
@RequestMapping("/api/users")
public class UserController {

    /**
     * @Valid kích hoạt validation trên CreateUserRequest.
     * Nếu validation fail → Spring ném MethodArgumentNotValidException
     * TRƯỚC KHI method body chạy.
     */
    @PostMapping
    public ResponseEntity<?> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        // Code ở đây CHỈ CHẠY khi validation PASS
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(userService.createUser(request));
    }

    /**
     * @Validated = @Valid + hỗ trợ Validation Groups.
     * Dùng khi cần validate khác nhau cho create vs update.
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateUser(
            @PathVariable Long id,
            @Validated(UpdateGroup.class) @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(userService.updateUser(id, request));
    }
}
```

---

## 4. Validation Groups

> Khi **create** cần validate khác **update** (ví dụ: create bắt buộc password, update thì không).

```java
// ===== Định nghĩa groups (marker interfaces) =====
public interface CreateGroup {}
public interface UpdateGroup {}

// ===== DTO dùng groups =====
public class UserRequest {

    @NotBlank(groups = {CreateGroup.class, UpdateGroup.class})
    private String username;

    @NotBlank(groups = CreateGroup.class)  // Chỉ bắt buộc khi create
    private String password;

    @Email(groups = {CreateGroup.class, UpdateGroup.class})
    private String email;
}

// ===== Controller dùng @Validated =====
@PostMapping
public ResponseEntity<?> create(
        @Validated(CreateGroup.class) @RequestBody UserRequest req) { ... }

@PutMapping("/{id}")
public ResponseEntity<?> update(
        @PathVariable Long id,
        @Validated(UpdateGroup.class) @RequestBody UserRequest req) { ... }
```

---

## 5. Custom Validator

### 5.1 Tạo Custom Annotation

```java
// ===== Annotation =====
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = StrongPasswordValidator.class)
public @interface StrongPassword {
    String message() default "Password phải ≥8 ký tự, có chữ hoa, thường, số và ký tự đặc biệt";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

// ===== Validator logic =====
public class StrongPasswordValidator
        implements ConstraintValidator<StrongPassword, String> {

    @Override
    public boolean isValid(String pw, ConstraintValidatorContext ctx) {
        if (pw == null) return false;
        return pw.length() >= 8
            && pw.matches(".*[A-Z].*")     // có chữ hoa
            && pw.matches(".*[a-z].*")     // có chữ thường
            && pw.matches(".*\\d.*")       // có số
            && pw.matches(".*[!@#$%^&*()].*"); // có ký tự đặc biệt
    }
}

// ===== Sử dụng =====
public class RegisterRequest {
    @NotBlank
    private String username;

    @StrongPassword   // Custom annotation
    private String password;
}
```

### 5.2 Custom Validator Với Dependency Injection

```java
// Validator có thể inject Spring beans!
public class UniqueUsernameValidator
        implements ConstraintValidator<UniqueUsername, String> {

    @Autowired
    private UserRepository userRepository;  // Inject repository

    @Override
    public boolean isValid(String username, ConstraintValidatorContext ctx) {
        if (username == null) return true;  // @NotBlank handle null
        return !userRepository.existsByUsername(username);
    }
}
```

### 5.3 Cross-Field Validation (Class-Level)

```java
// Validate 2 field liên quan: password == confirmPassword
@Target(ElementType.TYPE)  // Đặt trên class, không phải field
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PasswordMatchValidator.class)
public @interface PasswordMatch {
    String message() default "Password không khớp";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}

public class PasswordMatchValidator
        implements ConstraintValidator<PasswordMatch, RegisterRequest> {

    @Override
    public boolean isValid(RegisterRequest req, ConstraintValidatorContext ctx) {
        if (req.getPassword() == null) return true;
        boolean valid = req.getPassword().equals(req.getConfirmPassword());
        if (!valid) {
            ctx.disableDefaultConstraintViolation();
            ctx.buildConstraintViolationWithTemplate("Password không khớp")
               .addPropertyNode("confirmPassword")
               .addConstraintViolation();
        }
        return valid;
    }
}

@PasswordMatch  // Đặt trên class
public class RegisterRequest {
    @NotBlank private String username;
    @StrongPassword private String password;
    @NotBlank private String confirmPassword;
}
```

---

## 6. Xử Lý Lỗi Validation — Tích Hợp @ControllerAdvice

> **Tích hợp từ**: Phase 2.3.4 (GlobalExceptionHandling)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Bắt lỗi validation từ @Valid/@Validated trên @RequestBody.
     * MethodArgumentNotValidException chứa danh sách FieldError.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(err ->
            errors.put(err.getField(), err.getDefaultMessage())
        );

        return ResponseEntity.badRequest().body(Map.of(
            "status", 400,
            "error", "Validation Failed",
            "errors", errors,
            "timestamp", LocalDateTime.now().toString()
        ));
    }

    /**
     * Bắt lỗi validation từ @PathVariable, @RequestParam.
     * ConstraintViolationException (khác class ở trên!).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraint(
            ConstraintViolationException ex) {

        Map<String, String> errors = new LinkedHashMap<>();
        ex.getConstraintViolations().forEach(v ->
            errors.put(v.getPropertyPath().toString(), v.getMessage())
        );

        return ResponseEntity.badRequest().body(Map.of(
            "status", 400,
            "error", "Constraint Violation",
            "errors", errors
        ));
    }
}
```

**Response khi validation fail:**
```json
{
  "status": 400,
  "error": "Validation Failed",
  "errors": {
    "username": "Username không được để trống",
    "email": "Email không hợp lệ",
    "password": "Password phải ≥8 ký tự, có chữ hoa, thường, số và ký tự đặc biệt"
  },
  "timestamp": "2026-04-28T12:00:00"
}
```

---

## 7. Validate Nested Objects & Collections

```java
public class CreateOrderRequest {

    @NotBlank
    private String customerName;

    @Valid  // ← BẮT BUỘC để validate nested object
    @NotNull
    private AddressDTO shippingAddress;

    @Valid  // ← Validate từng phần tử trong list
    @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
    private List<OrderItemDTO> items;
}

public class AddressDTO {
    @NotBlank private String street;
    @NotBlank private String city;
    @Pattern(regexp = "\\d{5}", message = "Zip code phải 5 chữ số")
    private String zipCode;
}

public class OrderItemDTO {
    @NotNull private Long productId;
    @Positive(message = "Số lượng phải > 0")
    private Integer quantity;
}
```

> **QUAN TRỌNG**: Không có `@Valid` trên nested field → các annotation bên trong bị **bỏ qua**!

---

## 8. Validate Path Variables & Request Params

```java
@RestController
@RequestMapping("/api/users")
@Validated  // ← BẮT BUỘC ở class level cho @PathVariable/@RequestParam
public class UserController {

    @GetMapping("/{id}")
    public ResponseEntity<?> getUser(
            @PathVariable @Min(1) Long id) { ... }

    @GetMapping
    public ResponseEntity<?> search(
            @RequestParam @Size(min = 2) String keyword,
            @RequestParam @Min(0) int page,
            @RequestParam @Max(100) int size) { ... }
}
```

---

## 9. Validate Ở Service Layer

```java
@Service
@Validated  // Kích hoạt validation ở service
public class UserService {

    // Spring validate parameter trước khi method chạy
    public UserDTO findById(@Min(1) Long id) { ... }

    // Validate DTO ở service (defense in depth)
    public UserDTO createUser(@Valid CreateUserRequest request) { ... }
}
```

---

## ✅ Checklist

- [ ] Hiểu @NotNull vs @NotEmpty vs @NotBlank
- [ ] Dùng @Valid trên @RequestBody
- [ ] Tạo Custom Validator (annotation + logic)
- [ ] Cross-field validation (class-level)
- [ ] Validation Groups cho create vs update
- [ ] Xử lý MethodArgumentNotValidException trong @ControllerAdvice
- [ ] @Valid trên nested objects
- [ ] @Validated ở class level cho @PathVariable/@RequestParam

---

> **Tiếp theo**: Đọc `Phase5.2_DesignPatterns.md` →
