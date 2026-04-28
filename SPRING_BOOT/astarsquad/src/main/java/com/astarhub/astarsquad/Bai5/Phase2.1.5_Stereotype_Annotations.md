# 📚 Bài 5: Stereotype Annotations (@Component, @Service, @Repository, @Controller)

---

## 📑 Mục Lục

- [🎯 Mục tiêu](#mục-tiêu)
- [1. Stereotype Annotations là gì?](#1-stereotype-annotations-là-gì)
- [2. @Component - Annotation gốc](#2-component-annotation-gốc)
  - [Khi nào dùng @Component?](#khi-nào-dùng-component)
- [3. @Service - Business Logic Layer](#3-service-business-logic-layer)
  - [Đặc điểm @Service:](#đặc-điểm-service)
- [4. @Repository - Data Access Layer](#4-repository-data-access-layer)
  - [Đặc điểm @Repository:](#đặc-điểm-repository)
  - [Bonus: Exception Translation](#bonus-exception-translation)
- [5. @Controller - Presentation Layer](#5-controller-presentation-layer)
  - [@Controller (trả về View):](#controller-trả-về-view)
  - [@RestController (trả về JSON):](#restcontroller-trả-về-json)
  - [So sánh:](#so-sánh)
- [6. Layered Architecture](#6-layered-architecture)
  - [Các layer hoạt động cùng nhau:](#các-layer-hoạt-động-cùng-nhau)
- [7. Ví dụ đầy đủ](#7-ví-dụ-đầy-đủ)
  - [Product.java (Entity/Model):](#productjava-entitymodel)
  - [ProductRepository.java:](#productrepositoryjava)
  - [ProductService.java:](#productservicejava)
  - [ProductController.java:](#productcontrollerjava)
- [8. Tại sao phân biệt 4 annotation?](#8-tại-sao-phân-biệt-4-annotation)
  - [1. Dễ đọc code:](#1-dễ-đọc-code)
  - [2. Spring xử lý khác nhau:](#2-spring-xử-lý-khác-nhau)
  - [3. AOP (Aspect-Oriented Programming):](#3-aop-aspect-oriented-programming)
- [9. Quy tắc đặt tên](#9-quy-tắc-đặt-tên)
- [10. Lỗi hay gặp](#10-lỗi-hay-gặp)
  - [❌ Đặt business logic trong Controller:](#đặt-business-logic-trong-controller)
  - [✅ Chuyển business logic sang Service:](#chuyển-business-logic-sang-service)
- [📌 Tóm tắt](#tóm-tắt)
  - [Luồng xử lý:](#luồng-xử-lý)
  - [Quy tắc nhớ:](#quy-tắc-nhớ)

---

## 🎯 Mục tiêu
Hiểu rõ **4 annotation cơ bản** để đăng ký Bean và khi nào dùng cái nào.

---

## 1. Stereotype Annotations là gì?

> **Stereotype = "Khuôn mẫu" - Annotation đánh dấu class là Bean**

```
┌─────────────────────────────────────────────────────────────┐
│                  STEREOTYPE ANNOTATIONS                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│                      @Component                              │
│                          │                                   │
│          ┌───────────────┼───────────────┐                  │
│          │               │               │                   │
│          ▼               ▼               ▼                   │
│     @Service       @Repository     @Controller               │
│                                                              │
│   (Business)      (Database)       (HTTP)                   │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

**Bản chất:** 4 annotation này **GIỐNG NHAU** về mặt kỹ thuật - đều đăng ký class là Bean.

**Khác nhau:** Semantic (ý nghĩa) - giúp đọc code biết class thuộc layer nào.

---

## 2. @Component - Annotation gốc

> **@Component = "Đây là 1 Bean, Spring hãy quản lý nó"**

```java
@Component
public class EmailValidator {
    
    public boolean isValid(String email) {
        return email != null && email.contains("@");
    }
}
```

### Khi nào dùng @Component?

```
Khi class KHÔNG thuộc 3 loại còn lại:
- Không phải Service (business logic)
- Không phải Repository (database)
- Không phải Controller (HTTP)

Ví dụ:
- Validator
- Converter
- Helper/Utility class cần inject
- Scheduler
- Event Listener
```

---

## 3. @Service - Business Logic Layer

> **@Service = "Đây là Bean chứa business logic"**

```java
@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public Product createProduct(String name, double price) {
        // Business logic: validate, tính toán...
        if (price < 0) {
            throw new IllegalArgumentException("Giá không được âm");
        }
        
        Product product = new Product(name, price);
        return productRepository.save(product);
    }
    
    public double calculateDiscount(Product product, int percent) {
        // Business logic
        return product.getPrice() * (100 - percent) / 100;
    }
}
```

### Đặc điểm @Service:

```
✅ Chứa business logic (xử lý nghiệp vụ)
✅ Gọi Repository để truy cập database
✅ Được gọi từ Controller
✅ Thường đặt tên: XxxService
```

---

## 4. @Repository - Data Access Layer

> **@Repository = "Đây là Bean truy cập database"**

```java
@Repository
public class ProductRepository {
    
    private final JdbcTemplate jdbcTemplate;
    
    public ProductRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public Product save(Product product) {
        jdbcTemplate.update(
            "INSERT INTO products (name, price) VALUES (?, ?)",
            product.getName(), product.getPrice()
        );
        return product;
    }
    
    public List<Product> findAll() {
        return jdbcTemplate.query(
            "SELECT * FROM products",
            (rs, row) -> new Product(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getDouble("price")
            )
        );
    }
}
```

### Đặc điểm @Repository:

```
✅ Truy cập database (CRUD)
✅ Được gọi từ Service
✅ Thường đặt tên: XxxRepository
✅ Spring tự động translate SQL exceptions
```

### Bonus: Exception Translation

```java
@Repository  // ← Spring tự động bắt SQLException và chuyển thành DataAccessException
public class ProductRepository {
    
    public void save(Product product) {
        // Nếu có SQLException → Spring chuyển thành DataAccessException
        // Giúp code không phụ thuộc vào loại database cụ thể
    }
}
```

---

## 5. @Controller - Presentation Layer

> **@Controller = "Đây là Bean xử lý HTTP request"**

### @Controller (trả về View):

```java
@Controller  // Trả về tên view (HTML)
public class WebController {
    
    private final ProductService productService;
    
    public WebController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping("/products")
    public String listProducts(Model model) {
        model.addAttribute("products", productService.findAll());
        return "product-list";  // → Tìm file product-list.html
    }
}
```

### @RestController (trả về JSON):

```java
@RestController  // = @Controller + @ResponseBody
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    public List<Product> getAll() {
        return productService.findAll();  // → Trả về JSON
    }
}
```

### So sánh:

| | @Controller | @RestController |
|---|-------------|-----------------|
| **Trả về** | View name (HTML) | Data (JSON) |
| **Dùng cho** | Web truyền thống | REST API |
| **Response** | Render template | Serialize object |

---

## 6. Layered Architecture

### Các layer hoạt động cùng nhau:

```
┌─────────────────────────────────────────────────────────────┐
│                        CLIENT                                │
│                   (Browser / Mobile)                         │
└───────────────────────────┬─────────────────────────────────┘
                            │ HTTP Request
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                    @Controller / @RestController             │
│                    (Presentation Layer)                      │
│                                                              │
│   - Nhận HTTP request                                       │
│   - Validate input                                          │
│   - Gọi Service                                             │
│   - Trả về response                                         │
└───────────────────────────┬─────────────────────────────────┘
                            │ method call
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                         @Service                             │
│                    (Business Layer)                          │
│                                                              │
│   - Xử lý business logic                                    │
│   - Validate business rules                                 │
│   - Gọi Repository                                          │
│   - Transaction management                                  │
└───────────────────────────┬─────────────────────────────────┘
                            │ method call
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                       @Repository                            │
│                    (Data Access Layer)                       │
│                                                              │
│   - CRUD operations                                         │
│   - Query database                                          │
│   - Map result to object                                    │
└───────────────────────────┬─────────────────────────────────┘
                            │ SQL
                            ▼
┌─────────────────────────────────────────────────────────────┐
│                        DATABASE                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 7. Ví dụ đầy đủ

### Product.java (Entity/Model):

```java
public class Product {
    private Long id;
    private String name;
    private double price;
    
    // Constructor, Getters, Setters
}
```

### ProductRepository.java:

```java
@Repository
public class ProductRepository {
    
    private final List<Product> products = new ArrayList<>();
    private Long nextId = 1L;
    
    public Product save(Product product) {
        product.setId(nextId++);
        products.add(product);
        return product;
    }
    
    public List<Product> findAll() {
        return products;
    }
    
    public Optional<Product> findById(Long id) {
        return products.stream()
            .filter(p -> p.getId().equals(id))
            .findFirst();
    }
}
```

### ProductService.java:

```java
@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }
    
    public Product createProduct(String name, double price) {
        // Business validation
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Tên không được trống");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Giá không được âm");
        }
        
        Product product = new Product(name, price);
        return productRepository.save(product);
    }
    
    public List<Product> findAll() {
        return productRepository.findAll();
    }
    
    public Product findById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));
    }
}
```

### ProductController.java:

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping
    public List<Product> getAll() {
        return productService.findAll();
    }
    
    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productService.findById(id);
    }
    
    @PostMapping
    public Product create(@RequestBody Product product) {
        return productService.createProduct(product.getName(), product.getPrice());
    }
}
```

---

## 8. Tại sao phân biệt 4 annotation?

### 1. Dễ đọc code:

```java
// Nhìn annotation biết ngay class làm gì
@Service      → "À, đây là business logic"
@Repository   → "À, đây là truy cập database"
@Controller   → "À, đây là xử lý HTTP"
```

### 2. Spring xử lý khác nhau:

```java
@Repository  → Spring tự động translate SQLException
@Controller  → Spring xử lý HTTP request/response
@Service     → Có thể áp dụng @Transactional
```

### 3. AOP (Aspect-Oriented Programming):

```java
// Có thể áp dụng aspect cho từng layer
@Around("@within(org.springframework.stereotype.Service)")
public Object logService(ProceedingJoinPoint jp) {
    // Log tất cả method trong @Service
}
```

---

## 9. Quy tắc đặt tên

| Layer | Annotation | Tên class | Ví dụ |
|-------|------------|-----------|-------|
| **Presentation** | @Controller | XxxController | ProductController |
| **Business** | @Service | XxxService | ProductService |
| **Data Access** | @Repository | XxxRepository | ProductRepository |
| **Other** | @Component | Tùy context | EmailValidator |

---

## 10. Lỗi hay gặp

### ❌ Đặt business logic trong Controller:

```java
@RestController
public class ProductController {
    
    @PostMapping("/products")
    public Product create(@RequestBody Product product) {
        // ❌ SAI - Business logic ở Controller
        if (product.getPrice() < 0) {
            throw new Exception("Giá âm");
        }
        return repository.save(product);
    }
}
```

### ✅ Chuyển business logic sang Service:

```java
@RestController
public class ProductController {
    
    private final ProductService productService;
    
    @PostMapping("/products")
    public Product create(@RequestBody Product product) {
        // ✅ ĐÚNG - Gọi Service
        return productService.createProduct(product);
    }
}

@Service
public class ProductService {
    
    public Product createProduct(Product product) {
        // Business logic ở đây
        if (product.getPrice() < 0) {
            throw new IllegalArgumentException("Giá âm");
        }
        return repository.save(product);
    }
}
```

---

## 📌 Tóm tắt

| Annotation | Layer | Chức năng | Ví dụ |
|------------|-------|-----------|-------|
| **@Component** | General | Bean chung | Validator, Helper |
| **@Service** | Business | Xử lý nghiệp vụ | ProductService |
| **@Repository** | Data | Truy cập DB | ProductRepository |
| **@Controller** | Presentation | Xử lý HTTP | ProductController |

### Luồng xử lý:

```
Client → Controller → Service → Repository → Database
                ↓         ↓           ↓
            Validate   Business    CRUD
             Input      Logic      Data
```

### Quy tắc nhớ:

```
@Controller  = Nhận request, trả response
@Service     = Xử lý logic, gọi repository
@Repository  = Đọc/ghi database
@Component   = Còn lại (không thuộc 3 loại trên)
```

---

**Bài tiếp theo:** Project Structure (Cấu trúc thư mục dự án Spring Boot)
