# 📚 Bài 7: Configuration & Lombok

---

## 🎯 Mục tiêu
Hiểu cách **cấu hình ứng dụng** với properties/yml, **Profiles** cho các môi trường, và **Lombok** để giảm boilerplate code.

---

# PHẦN 1: application.properties / application.yml

---

## 1. File cấu hình là gì?

> **File cấu hình = Nơi lưu các thiết lập của ứng dụng**

```
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION                               │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   Cần biết:                                                 │
│   - Database nào? (URL, username, password)                 │
│   - Port nào?                                               │
│   - Log level?                                              │
│   - API key?                                                │
│                                                              │
│              ↓ Đọc từ file cấu hình                         │
│                                                              │
│   ┌─────────────────────────────────────┐                   │
│   │    application.properties           │                   │
│   │    hoặc application.yml             │                   │
│   └─────────────────────────────────────┘                   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. Hai định dạng: .properties vs .yml

### application.properties:

```properties
# Server
server.port=8080

# Database
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=123456

# JPA
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### application.yml (YAML):

```yaml
# Server
server:
  port: 8080

# Database
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/mydb
    username: root
    password: 123456
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: true
```

### So sánh:

| | .properties | .yml |
|---|-------------|------|
| **Cú pháp** | key=value | Thụt lề (indentation) |
| **Đọc** | Dễ với người mới | Gọn hơn, có cấu trúc |
| **Lặp prefix** | Có lặp | Không lặp |
| **Phổ biến** | Truyền thống | Xu hướng mới |

**Khuyên dùng:** Chọn 1 trong 2 cho cả dự án, đừng mix.

---

## 3. Các cấu hình phổ biến

### Server:

```properties
server.port=8080
server.servlet.context-path=/api
server.error.include-message=always
```

### Database:

```properties
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/mydb
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Connection Pool
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
```

### JPA/Hibernate:

```properties
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
```

| ddl-auto | Ý nghĩa |
|----------|---------|
| `none` | Không làm gì |
| `validate` | Chỉ validate schema |
| `update` | Tự động update schema |
| `create` | Tạo mới mỗi lần start |
| `create-drop` | Tạo mới và xóa khi stop |

### Logging:

```properties
logging.level.root=INFO
logging.level.com.example.myproject=DEBUG
logging.level.org.hibernate.SQL=DEBUG
logging.file.name=logs/app.log
```
## Các Log Level (từ thấp → cao):
Level	Khi nào dùng
TRACE	Chi tiết nhất, debug sâu
DEBUG	Thông tin debug khi dev
INFO	Thông tin chung, hoạt động bình thường
WARN	Cảnh báo, có thể có vấn đề
ERROR	Lỗi nghiêm trọng


---

## 4. Đọc cấu hình trong code

### Cách 1: @Value (đơn giản)

```java
@Service
public class EmailService {
    
    @Value("${app.email.from}")
    private String fromEmail;
    
    @Value("${app.email.enabled:true}")  // Có default value
    private boolean enabled;
    
    public void send(String to, String content) {
        if (enabled) {
            // Gửi email từ fromEmail
        }
    }
}
```

```properties
# application.properties
app.email.from=noreply@example.com
app.email.enabled=true
```

### Cách 2: @ConfigurationProperties (khuyên dùng)

```java
@Configuration
@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {
    
    private String from;
    private boolean enabled;
    private Smtp smtp;
    
    // Getters và Setters
    
    public static class Smtp {
        private String host;
        private int port;
        // Getters và Setters
    }
}
```

```properties
app.email.from=noreply@example.com
app.email.enabled=true
app.email.smtp.host=smtp.gmail.com
app.email.smtp.port=587
```

```java
@Service
public class EmailService {
    
    private final EmailProperties emailProperties;
    
    public EmailService(EmailProperties emailProperties) {
        this.emailProperties = emailProperties;
    }
    
    public void send() {
        String from = emailProperties.getFrom();
        String host = emailProperties.getSmtp().getHost();
        // ...
    }
}
```

### So sánh:

| | @Value | @ConfigurationProperties |
|---|--------|--------------------------|
| **Đơn giản** | ✅ | Phức tạp hơn |
| **Type-safe** | ❌ | ✅ |
| **Nhóm config** | ❌ | ✅ |
| **Dùng khi** | 1-2 properties | Nhiều properties liên quan |

---

## 5. Custom Properties

```properties
# Custom properties cho ứng dụng của bạn
app.name=My Awesome App
app.version=1.0.0
app.api-key=abc123xyz
app.max-upload-size=10MB
app.features.new-ui=true
app.features.dark-mode=false
```

---

# PHẦN 2: Profiles (Môi trường)

---

## 6. Profile là gì?

> **Profile = Bộ cấu hình cho từng môi trường (dev, test, prod)**

```
┌─────────────────────────────────────────────────────────────┐
│                      PROFILES                                │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   Development (dev)     Test           Production (prod)    │
│   ┌───────────────┐  ┌───────────────┐  ┌───────────────┐   │
│   │ localhost DB  │  │ Test DB       │  │ Production DB │   │
│   │ DEBUG log     │  │ Mock services │  │ INFO log      │   │
│   │ Port 8080     │  │ Port 8081     │  │ Port 80       │   │
│   └───────────────┘  └───────────────┘  └───────────────┘   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 7. Tạo Profile files

### Cấu trúc:

```
resources/
├── application.properties          ← Cấu hình chung
├── application-dev.properties      ← Cấu hình cho dev
├── application-test.properties     ← Cấu hình cho test
└── application-prod.properties     ← Cấu hình cho prod
```

### application.properties (chung):

```properties
# Cấu hình chung cho tất cả môi trường
app.name=My Application
spring.jpa.show-sql=true
```

### application-dev.properties:

```properties
# Development
server.port=8080
spring.datasource.url=jdbc:mysql://localhost:3306/mydb_dev
spring.datasource.username=root
spring.datasource.password=123456
spring.jpa.hibernate.ddl-auto=update
logging.level.com.example=DEBUG
```

### application-prod.properties:

```properties
# Production
server.port=80
spring.datasource.url=jdbc:mysql://prod-server:3306/mydb_prod
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=validate
logging.level.com.example=INFO
```

---

## 8. Kích hoạt Profile

### Cách 1: Trong application.properties:

```properties
spring.profiles.active=dev
```

### Cách 2: Command line:

```bash
# Maven
mvn spring-boot:run -Dspring-boot.run.profiles=dev

# Hoặc khi chạy JAR
java -jar app.jar --spring.profiles.active=prod
```

### Cách 3: Environment variable:

```bash
export SPRING_PROFILES_ACTIVE=prod
java -jar app.jar
```

### Cách 4: IDE (IntelliJ/VS Code):

```
Run Configuration → Environment variables → SPRING_PROFILES_ACTIVE=dev
```

---

## 9. Ưu tiên cấu hình

```
Ưu tiên từ CAO → THẤP:
──────────────────────
1. Command line arguments (--server.port=9090)
2. Environment variables (SERVER_PORT=9090)
3. application-{profile}.properties
4. application.properties
5. Default values trong code
```

---

## 10. @Profile - Bean theo môi trường

```java
// Bean chỉ tồn tại ở môi trường dev
@Service
@Profile("dev")
public class MockPaymentService implements PaymentService {
    public void pay() {
        System.out.println("Mock payment - dev only");
    }
}

// Bean chỉ tồn tại ở môi trường prod
@Service
@Profile("prod")
public class RealPaymentService implements PaymentService {
    public void pay() {
        // Gọi payment gateway thật
    }
}
```

```
Profile = dev  → MockPaymentService được tạo
Profile = prod → RealPaymentService được tạo
```

### Các cách dùng @Profile:

```java
@Profile("dev")           // Chỉ dev
@Profile("!prod")         // Không phải prod
@Profile({"dev", "test"}) // dev HOẶC test
```

---

## 11. Sử dụng Environment Variables

### Trong application.properties:

```properties
# Đọc từ environment variable
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}

# Có default value
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/mydb}
```

### Tại sao dùng env var?

```
✅ Không commit password vào Git
✅ Dễ thay đổi khi deploy
✅ Bảo mật hơn
```

---

# PHẦN 3: Lombok

---

## 12. Lombok là gì?

> **Lombok = Library giúp giảm boilerplate code (getter, setter, constructor...)**

### Trước khi có Lombok:

```java
public class Product {
    private Long id;
    private String name;
    private double price;
    
    // Constructor
    public Product() {}
    
    public Product(Long id, String name, double price) {
        this.id = id;
        this.name = name;
        this.price = price;
    }
    
    // Getters
    public Long getId() { return id; }
    public String getName() { return name; }
    public double getPrice() { return price; }
    
    // Setters
    public void setId(Long id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPrice(double price) { this.price = price; }
    
    // toString
    @Override
    public String toString() {
        return "Product{id=" + id + ", name='" + name + "', price=" + price + "}";
    }
    
    // equals & hashCode
    @Override
    public boolean equals(Object o) { ... }
    @Override
    public int hashCode() { ... }
}
```

### Sau khi có Lombok:

```java
@Data
public class Product {
    private Long id;
    private String name;
    private double price;
}
```

**Từ ~50 dòng → 6 dòng!**

---

## 13. Cài đặt Lombok

### Maven (pom.xml):

```xml
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
</dependency>
```

### IDE Plugin:

```
IntelliJ: Settings → Plugins → Search "Lombok" → Install
VS Code: Extension "Lombok Annotations Support"
```

---

## 14. Các Annotation phổ biến

### @Getter / @Setter:

```java
@Getter
@Setter
public class Product {
    private Long id;
    private String name;
}

// Tự động tạo:
// getId(), getName()
// setId(), setName()
```

### @NoArgsConstructor / @AllArgsConstructor:

```java
@NoArgsConstructor   // Constructor không tham số
@AllArgsConstructor  // Constructor tất cả tham số
public class Product {
    private Long id;
    private String name;
}

// Tự động tạo:
// public Product() {}
// public Product(Long id, String name) {}
```

### @RequiredArgsConstructor:

```java
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;  // ← final → required
    private final EmailService emailService;            // ← final → required
    
    // Tự động tạo:
    // public ProductService(ProductRepository productRepository, EmailService emailService) {
    //     this.productRepository = productRepository;
    //     this.emailService = emailService;
    // }
}
```

**Rất hay dùng với Constructor Injection!**

### @ToString:

```java
@ToString
public class Product {
    private Long id;
    private String name;
}

// Tự động tạo:
// toString() → "Product(id=1, name=iPhone)"
```

### @EqualsAndHashCode:

```java
@EqualsAndHashCode
public class Product {
    private Long id;
    private String name;
}

// Tự động tạo equals() và hashCode()
```

### @Data (Combo):

```java
@Data  // = @Getter + @Setter + @ToString + @EqualsAndHashCode + @RequiredArgsConstructor
public class Product {
    private Long id;
    private String name;
    private double price;
}
```

### @Builder:

```java
@Builder
public class Product {
    private Long id;
    private String name;
    private double price;
}

// Sử dụng:
Product product = Product.builder()
    .id(1L)
    .name("iPhone")
    .price(999)
    .build();
```

### @Slf4j (Logging):

```java
@Slf4j
@Service
public class ProductService {
    
    public void process() {
        log.info("Processing...");
        log.debug("Debug info");
        log.error("Error occurred", exception);
    }
}

// Tự động tạo:
// private static final Logger log = LoggerFactory.getLogger(ProductService.class);
```

---

## 15. Kết hợp thực tế

### Entity:

```java
@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String name;
    private double price;
}
```

### DTO:

```java
@Data
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private double price;
}
```

### Service:

```java
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public Product create(ProductDTO dto) {
        log.info("Creating product: {}", dto.getName());
        
        Product product = Product.builder()
            .name(dto.getName())
            .price(dto.getPrice())
            .build();
            
        return productRepository.save(product);
    }
}
```

### Controller:

```java
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {
    
    private final ProductService productService;
    
    @PostMapping
    public Product create(@RequestBody ProductDTO dto) {
        return productService.create(dto);
    }
}
```

---

## 16. Lưu ý khi dùng Lombok

### ⚠️ @Data với Entity:

```java
// ⚠️ Cẩn thận với @Data trên Entity có relationship
@Data  // Có thể gây StackOverflow với @OneToMany, @ManyToMany
@Entity
public class Order {
    @OneToMany(mappedBy = "order")
    private List<OrderItem> items;  // toString() gọi items.toString() → vòng lặp!
}

// ✅ Giải pháp: Exclude khỏi toString
@ToString(exclude = "items")
@EqualsAndHashCode(exclude = "items")
```

### ⚠️ IDE cần plugin:

```
Không có plugin → IDE báo lỗi "cannot find symbol"
Có plugin       → IDE hiểu Lombok annotations
```

---

## 📌 Tóm tắt

### Configuration:

| File | Mô tả |
|------|-------|
| `application.properties` | Cấu hình chung |
| `application-{profile}.properties` | Cấu hình theo môi trường |
| `@Value` | Đọc 1-2 properties |
| `@ConfigurationProperties` | Đọc nhóm properties |

### Profiles:

```
spring.profiles.active=dev    → Dùng application-dev.properties
spring.profiles.active=prod   → Dùng application-prod.properties
```

### Lombok:

| Annotation | Tạo ra |
|------------|--------|
| `@Getter/@Setter` | Getter, Setter |
| `@NoArgsConstructor` | Constructor không tham số |
| `@AllArgsConstructor` | Constructor tất cả tham số |
| `@RequiredArgsConstructor` | Constructor cho final fields |
| `@Data` | Tất cả trên + toString, equals, hashCode |
| `@Builder` | Builder pattern |
| `@Slf4j` | Logger |

---

**Bài tiếp theo:** REST API (@RestController, @GetMapping, @PostMapping...)
