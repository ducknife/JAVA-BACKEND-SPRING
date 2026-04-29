# 🏗️ Phase 5.2: Design Patterns Trong Spring Boot

---

## 📑 Mục Lục

- [1. Tổng Quan Design Patterns](#1-tổng-quan-design-patterns)
- [2. DTO Pattern](#2-dto-pattern)
  - [2.1 DTO Là Gì?](#21-dto-là-gì)
  - [2.2 Mapping Entity ↔ DTO](#22-mapping-entity--dto)
  - [2.3 MapStruct — Auto Mapping](#23-mapstruct--auto-mapping)
- [3. Repository Pattern](#3-repository-pattern)
- [4. Service Layer Pattern](#4-service-layer-pattern)
- [5. Builder Pattern](#5-builder-pattern)
- [6. Factory Pattern](#6-factory-pattern)
- [7. Strategy Pattern](#7-strategy-pattern)
- [8. Tổng Kết: Patterns Trong Kiến Trúc Spring](#8-tổng-kết-patterns-trong-kiến-trúc-spring)
- [✅ Checklist](#-checklist)

---

## 1. Tổng Quan Design Patterns

**Design Pattern** = giải pháp tái sử dụng cho các vấn đề phổ biến trong thiết kế phần mềm.

```
Trong Spring Boot, bạn ĐÃ dùng nhiều pattern mà không biết:

@RestController  → Controller Pattern
@Service         → Service Layer Pattern
@Repository      → Repository Pattern
@Builder         → Builder Pattern
DI/IoC           → Dependency Injection Pattern
SecurityFilterChain → Chain of Responsibility Pattern
```

---

## 2. DTO Pattern

### 2.1 DTO Là Gì?

**DTO (Data Transfer Object)** = object chỉ chứa data, dùng để truyền giữa các tầng.

```
Không có DTO:
Client ←→ Controller ←→ Service ←→ Repository
              ↑
          Entity trực tiếp
          → Lộ password, internal fields
          → Circular reference (Entity có relationship)
          → Client thay đổi → Entity thay đổi

Có DTO:
Client ←→ [RequestDTO] ←→ Controller ←→ Service ←→ Repository
Client ←→ [ResponseDTO] ←→                          ↕
                                                   Entity
→ Client chỉ thấy fields cần thiết
→ Entity thay đổi → DTO giữ nguyên (backward compatible)
```

### 2.2 Mapping Entity ↔ DTO

```java
// ===== Entity =====
@Entity
public class User {
    private Long id;
    private String username;
    private String email;
    private String password;  // KHÔNG được lộ ra ngoài!
    private boolean active;
    private Set<Role> roles;
}

// ===== Response DTO — chỉ chứa data client cần =====
public record UserResponse(
    Long id,
    String username,
    String email,
    List<String> roles
) {
    // Factory method: Entity → DTO
    public static UserResponse from(User user) {
        return new UserResponse(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRoles().stream()
                .map(Role::getName).toList()
        );
        // password KHÔNG có ở đây!
    }
}

// ===== Request DTO — validate input =====
public record CreateUserRequest(
    @NotBlank @Size(min = 3, max = 50) String username,
    @Email String email,
    @NotBlank @Size(min = 8) String password
) {}

// ===== Service =====
@Service
public class UserService {
    public UserResponse createUser(CreateUserRequest req) {
        User user = User.builder()
            .username(req.username())
            .email(req.email())
            .password(passwordEncoder.encode(req.password()))
            .build();
        return UserResponse.from(userRepository.save(user));
    }
}
```

### 2.3 MapStruct — Auto Mapping

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct</artifactId>
    <version>1.5.5.Final</version>
</dependency>
<dependency>
    <groupId>org.mapstruct</groupId>
    <artifactId>mapstruct-processor</artifactId>
    <version>1.5.5.Final</version>
    <scope>provided</scope>
</dependency>
```

```java
@Mapper(componentModel = "spring")
public interface UserMapper {
    UserResponse toResponse(User user);
    User toEntity(CreateUserRequest request);
    
    // Custom mapping
    @Mapping(target = "roles", expression = "java(user.getRoles().stream().map(Role::getName).toList())")
    UserResponse toDetailResponse(User user);
}

// Sử dụng:
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserMapper userMapper;  // Inject mapper

    public UserResponse createUser(CreateUserRequest req) {
        User user = userMapper.toEntity(req);
        return userMapper.toResponse(userRepository.save(user));
    }
}
```

---

## 3. Repository Pattern

> **Đã dùng** từ Phase 3 (JpaRepository). Tóm tắt:

```
Repository Pattern tách biệt business logic khỏi data access logic.

Service → Repository (interface) → JPA Implementation → Database
          ↑ Service chỉ biết interface
          ↑ Không biết SQL, không biết database nào
```

```java
// Spring Data JPA tự generate implementation
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRoleName(@Param("roleName") String roleName);
}
```

**Custom Repository** khi cần logic phức tạp:

```java
// Interface
public interface UserRepositoryCustom {
    List<User> searchUsers(UserSearchCriteria criteria);
}

// Implementation (Spring tự detect bằng tên "Impl")
public class UserRepositoryCustomImpl implements UserRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public List<User> searchUsers(UserSearchCriteria criteria) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        // ... dynamic query
    }
}

// Kế thừa cả hai
public interface UserRepository 
    extends JpaRepository<User, Long>, UserRepositoryCustom {}
```

---

## 4. Service Layer Pattern

```
Controller (nhận request, validate, trả response)
     │
     ▼
Service (business logic, transaction, orchestration)
     │
     ▼
Repository (data access)
```

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)  // Default readonly cho GET
public class OrderService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final OrderMapper orderMapper;

    // Business logic = orchestrate nhiều repository + service
    @Transactional  // Override: cần write
    public OrderResponse createOrder(CreateOrderRequest request, Long userId) {
        // 1. Validate business rules
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new NotFoundException("User not found"));

        // 2. Business logic
        Order order = orderMapper.toEntity(request);
        order.setUser(user);
        order.setStatus(OrderStatus.PENDING);
        order.setOrderNumber(generateOrderNumber());

        // 3. Save
        Order saved = orderRepository.save(order);

        // 4. Side effects
        notificationService.sendOrderConfirmation(saved);

        // 5. Return DTO
        return orderMapper.toResponse(saved);
    }
}
```

---

## 5. Builder Pattern

> **Đã dùng** với Lombok `@Builder`. Giải thích bên trong:

```java
// Lombok @Builder tự generate code này:
public class User {
    private Long id;
    private String username;
    private String email;

    // Builder inner class
    public static UserBuilder builder() {
        return new UserBuilder();
    }

    public static class UserBuilder {
        private Long id;
        private String username;
        private String email;

        public UserBuilder id(Long id) { this.id = id; return this; }
        public UserBuilder username(String u) { this.username = u; return this; }
        public UserBuilder email(String e) { this.email = e; return this; }

        public User build() {
            return new User(id, username, email);
        }
    }
}

// Sử dụng — fluent API:
User user = User.builder()
    .username("admin")
    .email("admin@example.com")
    .build();
```

---

## 6. Factory Pattern

```java
// Factory cho tạo Notification theo type
@Component
@RequiredArgsConstructor
public class NotificationFactory {

    private final Map<String, NotificationSender> senders;
    // Spring tự inject tất cả bean implement NotificationSender
    // Key = bean name, Value = bean instance

    public NotificationSender getSender(NotificationType type) {
        String beanName = type.name().toLowerCase() + "NotificationSender";
        NotificationSender sender = senders.get(beanName);
        if (sender == null) throw new IllegalArgumentException(
            "Unsupported notification type: " + type);
        return sender;
    }
}

// Implementations
@Component("emailNotificationSender")
public class EmailNotificationSender implements NotificationSender {
    public void send(String to, String message) { /* email logic */ }
}

@Component("smsNotificationSender")
public class SmsNotificationSender implements NotificationSender {
    public void send(String to, String message) { /* sms logic */ }
}
```

---

## 7. Strategy Pattern

```java
// Interface chiến lược
public interface PricingStrategy {
    BigDecimal calculatePrice(Order order);
    boolean supports(CustomerType type);
}

@Component
public class RegularPricing implements PricingStrategy {
    public BigDecimal calculatePrice(Order order) { return order.getSubtotal(); }
    public boolean supports(CustomerType type) { return type == CustomerType.REGULAR; }
}

@Component
public class VipPricing implements PricingStrategy {
    public BigDecimal calculatePrice(Order order) {
        return order.getSubtotal().multiply(BigDecimal.valueOf(0.9)); // giảm 10%
    }
    public boolean supports(CustomerType type) { return type == CustomerType.VIP; }
}

// Service chọn strategy
@Service
@RequiredArgsConstructor
public class PricingService {
    private final List<PricingStrategy> strategies; // Spring inject tất cả

    public BigDecimal calculatePrice(Order order, CustomerType type) {
        return strategies.stream()
            .filter(s -> s.supports(type))
            .findFirst()
            .orElseThrow()
            .calculatePrice(order);
    }
}
```

---

## 8. Tổng Kết: Patterns Trong Kiến Trúc Spring

```
Client
  │
  ▼
Controller          → DTO Pattern (Request/Response DTO)
  │
  ▼
Service             → Service Layer, Strategy, Factory
  │
  ▼
Repository          → Repository Pattern
  │
  ▼
Entity/Database     → Builder Pattern
```

---

## ✅ Checklist

- [ ] Dùng DTO tách biệt Entity và API contract
- [ ] Dùng record cho DTO (Java 16+)
- [ ] Hiểu Repository Pattern + Custom Repository
- [ ] Service layer chứa business logic, không phải Controller
- [ ] Builder Pattern với Lombok @Builder
- [ ] Factory Pattern cho dynamic object creation
- [ ] Strategy Pattern cho multiple algorithms

---

> **Tiếp theo**: Đọc `Phase5.3_Testing.md` →
