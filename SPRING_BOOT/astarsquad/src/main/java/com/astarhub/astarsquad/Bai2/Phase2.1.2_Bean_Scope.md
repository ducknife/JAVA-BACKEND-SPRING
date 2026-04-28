# 📚 Bài 2: Bean & Bean Scope

---

## 🎯 Mục tiêu
Hiểu **Bean là gì**, các **loại Scope**, và khi nào dùng scope nào.

---

## 1. Bean là gì?

> **Bean = Object được Spring Container tạo và quản lý**

```java
// ❌ KHÔNG phải Bean - bạn tự tạo
Product product = new Product();

// ✅ LÀ Bean - Spring tạo và quản lý
@Service
public class ProductService { }
```

**Spring quản lý Bean nghĩa là:**
- Spring quyết định **khi nào tạo**
- Spring **inject dependencies**
- Spring gọi **lifecycle callbacks** (init, destroy)
- Spring quản lý **số lượng instance**

---

## 2. Cách đăng ký Bean

### Cách 1: Stereotype Annotations (phổ biến)

```java
@Component   // Bean chung chung
public class MyComponent { }

@Service     // Bean xử lý business logic
public class ProductService { }

@Repository  // Bean truy cập database
public class ProductRepository { }

@Controller  // Bean xử lý HTTP request
public class ProductController { }
```

**Bản chất:** 4 annotation này **giống nhau**, đều đánh dấu class là Bean.
**Khác nhau:** Semantic (ý nghĩa) - giúp đọc code biết class thuộc layer nào.

### Cách 2: @Bean trong @Configuration

#### 🤔 Tại sao cần cách này?

**Vấn đề:** Bạn muốn dùng class từ thư viện (VD: `RestTemplate`, `ObjectMapper`) như Bean, nhưng **không thể dán @Service** lên class đó vì bạn không sở hữu source code.

```java
@Service  // ❌ KHÔNG THỂ! Đây là class của Spring, không phải của bạn
public class RestTemplate { }
```

**Giải pháp:** Viết method trả về object đó, Spring sẽ quản lý object đó như Bean.

```java
@Configuration  // Đánh dấu: "File này chứa cấu hình"
public class AppConfig {
    
    @Bean  // Đánh dấu: "Object trả về từ method này là Bean"
    public RestTemplate restTemplate() {
        return new RestTemplate();  // ← Object này thành Bean
    }
}
```

#### 📝 Ví dụ thực tế: Mã hóa password

```java
// Bước 1: Tạo Bean trong Configuration
@Configuration // ← Báo cho spring biết class này chứa các cài đặt hệ thống
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();  // Class của Spring Security
    }
}

// Bước 2: Inject và sử dụng bình thường
@Service
public class UserService {
    
    private final PasswordEncoder passwordEncoder;
    
    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;  // ← Spring inject Bean
    }
    
    public String hashPassword(String raw) {
        return passwordEncoder.encode(raw);
    }
}
```

#### ✅ Khi nào dùng @Bean?

| Tình huống | Ví dụ |
|------------|-------|
| Class từ thư viện | `RestTemplate`, `ObjectMapper`, `PasswordEncoder` |
| Cần cấu hình khi tạo | Set timeout, set options... |
| Tạo nhiều Bean cùng loại | `@Bean("mysql")`, `@Bean("postgres")` |

#### 📌 Quy tắc nhớ:

```
Class CỦA BẠN        →  Dán @Service, @Component lên class
Class KHÔNG của bạn  →  Viết @Bean method trong @Configuration
```

---

## 3. Bean Scope

> **Scope = Phạm vi sống của Bean, quyết định số lượng instance**

### 🤔 Tại sao cần hiểu Scope?

Hãy tưởng tượng:
- **ProductService** - Chỉ cần 1 instance, dùng chung cho cả app ✅
- **ShoppingCart** - Mỗi khách hàng cần 1 giỏ hàng riêng ❌ (nếu dùng chung sẽ loạn)
Scope giúp bạn kiểm soát **bao nhiêu instance** được tạo.
---

### Singleton (Mặc định) - 1 instance duy nhất

#### Định nghĩa:

```java
@Service  // Mặc định là Singleton, không cần ghi @Scope
public class ProductService {
    
    public ProductService() {
        System.out.println("ProductService được tạo!");
    }
    
    public void doSomething() {
        System.out.println("Processing...");
    }
}
```

#### Cách hoạt động:
```
Ứng dụng start
     │
     ▼
Spring tạo ProductService (1 lần duy nhất)
     │
     ├──► OrderController inject → nhận instance A
     ├──► UserController inject  → nhận instance A (CÙNG object)
     └──► ReportService inject   → nhận instance A (CÙNG object)
```

#### Ví dụ minh họa (Constructor Injection ✅):

```java
@Service
public class ProductService {
    private int counter = 0;
    public void process() {
        counter++;
        System.out.println("Counter: " + counter);
    }
}

@Component
public class Demo implements CommandLineRunner {
    
    private final ProductService service1;
    private final ProductService service2;
    
    // ✅ Constructor Injection (khuyên dùng)
    public Demo(ProductService service1, ProductService service2) {
        this.service1 = service1;
        this.service2 = service2;
    }
    
    @Override
    public void run(String... args) {
        service1.process();  // Counter: 1
        service2.process();  // Counter: 2 ← CÙNG instance nên counter tăng tiếp!
        System.out.println(service1 == service2);  // true ← Cùng 1 object
    }
}
```

**Output:**
```
Counter: 1
Counter: 2
true
```

#### ⚠️ Lưu ý quan trọng: Singleton phải STATELESS

```java
// ❌ SAI - Lưu state trong Singleton (nguy hiểm!)
@Service
public class OrderService {
    private Order currentOrder;  // ⚠️ Nhiều user dùng chung → loạn data!
}

// ✅ ĐÚNG - Stateless, truyền data qua parameter
@Service
public class OrderService {
    public void process(Order order) {  // Mỗi request truyền order riêng
        // Xử lý order
    }
}
```

---

### Prototype - Mỗi lần inject tạo instance MỚI

#### 🤔 Khi nào cần Prototype?

Khi bạn cần **mỗi nơi sử dụng có instance riêng** (có state riêng).

| Singleton | Prototype |
|-----------|-----------|
| 1 instance dùng chung | Mỗi lần inject → instance mới |
| Stateless (không lưu state) | Có thể Stateful (lưu state) |
| VD: `ProductService` | VD: `ShoppingCart` |

#### 🎯 Ví dụ thực tế: Tại sao ShoppingCart cần Prototype?

```
Singleton ShoppingCart (SAI):
─────────────────────────────
User A thêm iPhone    → cart = [iPhone]
User B thêm MacBook   → cart = [iPhone, MacBook]  ← User B thấy iPhone của A!
User A thanh toán     → Thanh toán cả MacBook của B! 💥

Prototype ShoppingCart (ĐÚNG):
──────────────────────────────
User A inject cart    → cartA = []
User B inject cart    → cartB = []  (instance KHÁC)
User A thêm iPhone    → cartA = [iPhone]
User B thêm MacBook   → cartB = [MacBook]  ← Riêng biệt! ✅
```

#### Định nghĩa:

```java
@Component
@Scope("prototype")  // ← Mỗi lần inject tạo instance MỚI
public class ShoppingCart {
    
    private final String cartId;
    private final List<String> items = new ArrayList<>();
    
    public ShoppingCart() {
        this.cartId = UUID.randomUUID().toString().substring(0, 8);
        System.out.println("🛒 Tạo giỏ hàng mới: " + cartId);
    }
    
    public void addItem(String item) {
        items.add(item);
    }
    
    public List<String> getItems() {
        return items;
    }
    
    public String getCartId() {
        return cartId;
    }
}
```

#### Cách hoạt động:

```
┌─────────────────────────────────────────────────────────┐
│                    PROTOTYPE SCOPE                       │
└─────────────────────────────────────────────────────────┘

Service1 inject ShoppingCart 
    └──► Spring gọi new ShoppingCart() → instance A

Service2 inject ShoppingCart
    └──► Spring gọi new ShoppingCart() → instance B (MỚI!)

Service3 inject ShoppingCart
    └──► Spring gọi new ShoppingCart() → instance C (MỚI!)

→ Mỗi nơi inject đều có instance RIÊNG
```

#### Ví dụ minh họa (Constructor Injection ✅):

```java
@Component
public class Demo implements CommandLineRunner {
    
    private final ShoppingCart cart1;
    private final ShoppingCart cart2;
    
    // ✅ Constructor Injection
    public Demo(ShoppingCart cart1, ShoppingCart cart2) {
        this.cart1 = cart1;
        this.cart2 = cart2;
    }
    
    @Override
    public void run(String... args) {
        System.out.println("Cart1 ID: " + cart1.getCartId());
        System.out.println("Cart2 ID: " + cart2.getCartId());
        
        cart1.addItem("iPhone");
        cart2.addItem("MacBook");
        
        System.out.println("Cart1: " + cart1.getItems());  // [iPhone]
        System.out.println("Cart2: " + cart2.getItems());  // [MacBook]
        
        System.out.println(cart1 == cart2);  // false ← Khác object
    }
}
```

**Output:**
```
🛒 Tạo giỏ hàng mới: a1b2c3d4
🛒 Tạo giỏ hàng mới: e5f6g7h8
Cart1 ID: a1b2c3d4
Cart2 ID: e5f6g7h8
Cart1: [iPhone]
Cart2: [MacBook]
false
```

#### ⚠️ Lưu ý quan trọng với Prototype:

**1. Spring KHÔNG quản lý lifecycle sau khi tạo:**
```java
@Component
@Scope("prototype")
public class ShoppingCart {
    
    @PreDestroy  // ❌ KHÔNG BAO GIỜ ĐƯỢC GỌI!
    public void cleanup() {
        System.out.println("Cleanup...");
    }
}
```
→ Bạn phải tự cleanup nếu cần (đóng connection, giải phóng resource...)

**2. Mỗi lần inject = tạo mới, không phải mỗi lần gọi method:**
```java
@Service
public class OrderService {
    private final ShoppingCart cart;
    
    public OrderService(ShoppingCart cart) {
        this.cart = cart;  // Tạo 1 lần ở đây
    }
    
    public void process1() {
        cart.addItem("A");  // Dùng cart đã tạo
    }
    
    public void process2() {
        cart.addItem("B");  // Vẫn là cart cũ, KHÔNG tạo mới!
    }
}
```

**3. Muốn tạo mới mỗi lần gọi → dùng ObjectProvider:**
```java
@Service
public class OrderService {
    private final ObjectProvider<ShoppingCart> cartProvider;
    
    public OrderService(ObjectProvider<ShoppingCart> cartProvider) {
        this.cartProvider = cartProvider;
    }
    
    public void process() {
        ShoppingCart cart = cartProvider.getObject();  // Tạo mới mỗi lần!
        cart.addItem("iPhone");
    }
}
```

#### 📌 So sánh Singleton vs Prototype:

| Tiêu chí | Singleton | Prototype |
|----------|-----------|-----------|
| Số instance | 1 | Nhiều (mỗi inject = 1) |
| Khi nào tạo | App start | Mỗi lần inject |
| Lifecycle | Spring quản lý đầy đủ | Spring chỉ tạo, không destroy |
| State | KHÔNG nên có | CÓ THỂ có |
| Use case | Service, Repository | Cart, Builder, Report |
---

### Request Scope (Web) - 1 instance / HTTP request

#### Khi nào cần?

Mỗi HTTP request cần lưu thông tin riêng (request ID, start time...).

```java
@Component
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class RequestContext {
    
    private final String requestId = UUID.randomUUID().toString();
    private final LocalDateTime startTime = LocalDateTime.now();
    
    public String getRequestId() {
        return requestId;
    }
    
    public LocalDateTime getStartTime() {
        return startTime;
    }
}
```

#### Cách hoạt động:

```
Request 1 đến → Spring tạo RequestContext instance A
     │
     └── Controller, Service đều nhận instance A

Request 2 đến → Spring tạo RequestContext instance B (MỚI)
     │
     └── Controller, Service đều nhận instance B
```

#### Ví dụ sử dụng (Constructor Injection ✅):

```java
@RestController
public class ProductController {
    
    private final ProductService productService;
    private final RequestContext requestContext;
    
    public ProductController(ProductService productService, 
                             RequestContext requestContext) {
        this.productService = productService;
        this.requestContext = requestContext;
    }
    
    @GetMapping("/products")
    public List<Product> getProducts() {
        System.out.println("Request ID: " + requestContext.getRequestId());
        return productService.findAll();
    }
}
```

#### ❓ Tại sao cần `proxyMode`?

```java
@Scope(value = "request", proxyMode = ScopedProxyMode.TARGET_CLASS)
```

**Vấn đề:** Controller là Singleton (tạo 1 lần), RequestContext là Request scope (tạo mỗi request).

**Giải pháp:** Spring tạo **Proxy** - proxy này sẽ tự động "tìm đúng instance" cho mỗi request.

---

### Session Scope (Web) - 1 instance / HTTP session

#### Khi nào cần?

Lưu thông tin user trong suốt phiên đăng nhập.

```java
@Component
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class UserSession {
    
    private String username;
    private List<String> recentViews = new ArrayList<>();
    
    // getters, setters
}
```

#### Cách hoạt động:

```
User A login → Session A → UserSession instance A
     │
     ├── Request 1: nhận instance A
     ├── Request 2: nhận instance A (CÙNG session)
     └── Request 3: nhận instance A

User B login → Session B → UserSession instance B (KHÁC user)
```

---

## 4. Vấn đề: Inject Prototype vào Singleton

### ❌ Vấn đề:

```java
@Service  // Singleton - tạo 1 lần khi app start
public class OrderService {
    
    private final ShoppingCart cart;  // Prototype
    
    // Constructor Injection
    public OrderService(ShoppingCart cart) {
        this.cart = cart;  // Inject 1 lần duy nhất!
    }
    
    public void process() {
        cart.addItem("iPhone");
        // ⚠️ cart luôn là CÙNG 1 instance!
    }
}
```

### 🔍 Tại sao?

```
App start
    │
    ▼
Spring tạo ShoppingCart instance A
    │
    ▼
Spring tạo OrderService, inject cart = instance A
    │
    ▼
Suốt đời app: OrderService.cart = instance A (KHÔNG BAO GIỜ ĐỔI)
```

### ✅ Giải pháp: ObjectProvider, có thể dùng Optional 

```java
@Service
public class OrderService {
    
    private final ObjectProvider<ShoppingCart> cartProvider;
    
    public OrderService(ObjectProvider<ShoppingCart> cartProvider) {
        this.cartProvider = cartProvider;
    }
    
    public void processOrder(String product) {
        // Mỗi lần gọi getObject() → Spring tạo instance MỚI
        ShoppingCart cart = cartProvider.getObject();
        cart.addItem(product);
        System.out.println("Cart ID: " + cart.getCartId());
    }
}
```

### 🔍 Kết quả:

```
processOrder("iPhone")  → getObject() → instance A (mới)
processOrder("MacBook") → getObject() → instance B (mới)
processOrder("iPad")    → getObject() → instance C (mới)
```

---

## 5. Khi nào dùng Scope nào?

| Scope | Khi nào dùng | Ví dụ |
|-------|--------------|-------|
| **Singleton** | Stateless services, shared resources | `ProductService`, `EmailService` |
| **Prototype** | Stateful objects, cần instance mới mỗi lần | `ShoppingCart`, `ReportBuilder` |
| **Request** | Data trong 1 HTTP request | `RequestContext`, `RequestLogger` |
| **Session** | Data của user trong session | `UserSession`, `CartSession` |

---

## 📌 Tóm tắt

| Khái niệm | Ý nghĩa |
|-----------|---------|
| **Bean** | Object được Spring quản lý |
| **@Service, @Component...** | Đánh dấu class là Bean |
| **@Bean** | Tạo Bean trong @Configuration |
| **Singleton** | 1 instance cho toàn app (mặc định) |
| **Prototype** | Instance mới mỗi lần inject |
| **ObjectProvider** | Giải quyết inject Prototype vào Singleton |
| **Constructor Injection** | Cách inject được khuyên dùng |

---

**Bài tiếp theo:** Bean Lifecycle (@PostConstruct, @PreDestroy)
