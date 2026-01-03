# 📚 Bài 4: REST API cơ bản

---

## 🎯 Mục tiêu
Hiểu **REST API là gì** và cách tạo API với Spring Boot.

---

## 1. REST API là gì?

> **REST API = Cách để client (web, mobile) giao tiếp với server qua HTTP**

```
┌─────────────┐         HTTP Request          ┌─────────────┐
│   Client    │  ──────────────────────────►  │   Server    │
│  (Browser,  │                               │  (Spring    │
│   Mobile)   │  ◄──────────────────────────  │   Boot)     │
└─────────────┘         HTTP Response         └─────────────┘
```

### HTTP Methods (động từ):

| Method | Ý nghĩa | Ví dụ |
|--------|---------|-------|
| **GET** | Lấy dữ liệu | Xem danh sách sản phẩm |
| **POST** | Tạo mới | Thêm sản phẩm mới |
| **PUT** | Cập nhật toàn bộ | Sửa toàn bộ thông tin sản phẩm |
| **PATCH** | Cập nhật một phần | Sửa giá sản phẩm |
| **DELETE** | Xóa | Xóa sản phẩm |

### RESTful URL (danh từ):

```
GET    /products        → Lấy tất cả sản phẩm
GET    /products/1      → Lấy sản phẩm có id=1
POST   /products        → Tạo sản phẩm mới
PUT    /products/1      → Cập nhật sản phẩm id=1
DELETE /products/1      → Xóa sản phẩm id=1
```

**Quy tắc:** URL là danh từ (products), HTTP method là động từ (GET, POST...)

---

## 2. @RestController - Đánh dấu class xử lý API

```java
@RestController  // ← Đánh dấu: "Class này xử lý REST API"
public class ProductController {
    
}
```

### @RestController vs @Controller

| | @Controller | @RestController |
|---|-------------|-----------------|
| **Trả về** | View (HTML) | Data (JSON) |
| **Dùng cho** | Web truyền thống | REST API |
| **Bản chất** | @Controller | @Controller + @ResponseBody |

```java
// @Controller - Trả về tên view
@Controller
public class WebController {
    @GetMapping("/home")
    public String home() {
        return "home";  // → Tìm file home.html
    }
}

// @RestController - Trả về data (JSON)
@RestController
public class ApiController {
    @GetMapping("/api/products")
    public List<Product> products() {
        return productList;  // → Trả về JSON
    }
}
```

---

## 3. @RequestMapping - Định nghĩa base URL

```java
@RestController
@RequestMapping("/api/products")  // ← Base URL cho tất cả methods
public class ProductController {
    
    @GetMapping          // → GET /api/products
    public List<Product> getAll() { }
    
    @GetMapping("/{id}") // → GET /api/products/1
    public Product getById(@PathVariable Long id) { }
    
    @PostMapping         // → POST /api/products
    public Product create(@RequestBody Product product) { }
}
```

---

## 4. @GetMapping - Lấy dữ liệu

### Lấy tất cả:

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    @GetMapping  // GET /api/products
    public List<Product> getAll() {
        return productService.findAll();
    }
}
```

**Response:**
```json
[
    {"id": 1, "name": "iPhone", "price": 999},
    {"id": 2, "name": "MacBook", "price": 1999}
]
```

### Lấy theo ID với @PathVariable:

```java
@GetMapping("/{id}")  // GET /api/products/1
public Product getById(@PathVariable Long id) {
    return productService.findById(id);
}
```

```
URL: /api/products/1
                  ↓
         @PathVariable Long id = 1
```

### Lấy với query params với @RequestParam:

```java
@GetMapping("/search")  // GET /api/products/search?name=iphone&minPrice=500
public List<Product> search(
    @RequestParam String name,
    @RequestParam(required = false, defaultValue = "0") Double minPrice
) {
    return productService.search(name, minPrice);
}
```

```
URL: /api/products/search?name=iphone&minPrice=500
                          ↓              ↓
              @RequestParam name    @RequestParam minPrice
```

---

## 5. @PostMapping - Tạo mới

```java
@PostMapping  // POST /api/products
public Product create(@RequestBody Product product) {
    return productService.save(product);
}
```

### @RequestBody - Nhận data từ body request:

```
POST /api/products
Content-Type: application/json

{
    "name": "iPad",
    "price": 799
}
     ↓
@RequestBody Product product
     ↓
product.getName() = "iPad"
product.getPrice() = 799
```

---

## 6. @PutMapping - Cập nhật toàn bộ

```java
@PutMapping("/{id}")  // PUT /api/products/1
public Product update(
    @PathVariable Long id,
    @RequestBody Product product
) {
    return productService.update(id, product);
}
```

```
PUT /api/products/1
Content-Type: application/json

{
    "name": "iPhone 15",
    "price": 1099
}
```

---

## 7. @DeleteMapping - Xóa

```java
@DeleteMapping("/{id}")  // DELETE /api/products/1
public void delete(@PathVariable Long id) {
    productService.delete(id);
}
```

---

## 8. Ví dụ đầy đủ: ProductController

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {
    
    private final ProductService productService;
    
    public ProductController(ProductService productService) {
        this.productService = productService;
    }
    
    // GET /api/products
    @GetMapping
    public List<Product> getAll() {
        return productService.findAll();
    }
    
    // GET /api/products/1
    @GetMapping("/{id}")
    public Product getById(@PathVariable Long id) {
        return productService.findById(id);
    }
    
    // GET /api/products/search?name=iphone
    @GetMapping("/search")
    public List<Product> search(@RequestParam String name) {
        return productService.searchByName(name);
    }
    
    // POST /api/products
    @PostMapping
    public Product create(@RequestBody Product product) {
        return productService.save(product);
    }
    
    // PUT /api/products/1
    @PutMapping("/{id}")
    public Product update(
        @PathVariable Long id,
        @RequestBody Product product
    ) {
        return productService.update(id, product);
    }
    
    // DELETE /api/products/1
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
```

---

## 9. HTTP Status Code

### Các status phổ biến:

| Code | Ý nghĩa | Khi nào |
|------|---------|---------|
| **200** | OK | Thành công |
| **201** | Created | Tạo mới thành công |
| **204** | No Content | Xóa thành công |
| **400** | Bad Request | Request sai format |
| **404** | Not Found | Không tìm thấy |
| **500** | Server Error | Lỗi server |

### Cách trả về status code:

```java
// Cách 1: Dùng ResponseEntity
@PostMapping
public ResponseEntity<Product> create(@RequestBody Product product) {
    Product saved = productService.save(product);
    return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    // hoặc: return ResponseEntity.created(uri).body(saved);
}

@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    productService.delete(id);
    return ResponseEntity.noContent().build();  // 204
}

// Cách 2: Dùng @ResponseStatus
@PostMapping
@ResponseStatus(HttpStatus.CREATED)  // Luôn trả về 201
public Product create(@RequestBody Product product) {
    return productService.save(product);
}
```

---

## 10. ResponseEntity - Linh hoạt hơn

```java
@GetMapping("/{id}")
public ResponseEntity<Product> getById(@PathVariable Long id) {
    Product product = productService.findById(id);
    
    if (product == null) {
        return ResponseEntity.notFound().build();  // 404
    }
    
    return ResponseEntity.ok(product);  // 200 + data
}
```

### Các method tiện ích:

```java
ResponseEntity.ok(body)              // 200 + body
ResponseEntity.created(uri).body()   // 201 + body
ResponseEntity.noContent().build()   // 204
ResponseEntity.notFound().build()    // 404
ResponseEntity.badRequest().body()   // 400 + body
ResponseEntity.status(HttpStatus.XXX).body()  // Custom
```

---

## 11. @PathVariable vs @RequestParam

| | @PathVariable | @RequestParam |
|---|---------------|---------------|
| **Vị trí** | Trong URL path | Query string (?...) |
| **Ví dụ** | `/products/1` | `/products?id=1` |
| **Dùng khi** | ID, resource cụ thể | Filter, search, optional |

```java
// @PathVariable - Resource cụ thể
@GetMapping("/{id}")
public Product getById(@PathVariable Long id) { }
// URL: /products/1

// @RequestParam - Filter/Search
@GetMapping("/search")
public List<Product> search(
    @RequestParam String name,
    @RequestParam(required = false) Double maxPrice
) { }
// URL: /products/search?name=iphone&maxPrice=1000
```

---

## 12. @RequestBody vs @RequestParam

| | @RequestBody | @RequestParam |
|---|--------------|---------------|
| **Data ở đâu** | Body của request | Query string |
| **Format** | JSON | key=value |
| **Dùng khi** | POST, PUT (gửi object) | GET (filter, search) |

```java
// @RequestBody - Nhận JSON object
@PostMapping
public Product create(@RequestBody Product product) { }
// Body: {"name": "iPhone", "price": 999}

// @RequestParam - Nhận từ query string
@GetMapping("/search")
public List<Product> search(@RequestParam String name) { }
// URL: /products/search?name=iphone
```

---

## 📌 Tóm tắt

### Annotations:

| Annotation | Ý nghĩa |
|------------|---------|
| **@RestController** | Đánh dấu class xử lý REST API |
| **@RequestMapping** | Base URL cho controller |
| **@GetMapping** | Xử lý GET request |
| **@PostMapping** | Xử lý POST request |
| **@PutMapping** | Xử lý PUT request |
| **@DeleteMapping** | Xử lý DELETE request |
| **@PathVariable** | Lấy giá trị từ URL path |
| **@RequestParam** | Lấy giá trị từ query string |
| **@RequestBody** | Lấy data từ body (JSON) |

### CRUD mapping:

```
GET    /resources      → Lấy tất cả
GET    /resources/{id} → Lấy theo ID
POST   /resources      → Tạo mới
PUT    /resources/{id} → Cập nhật
DELETE /resources/{id} → Xóa
```

---

**Bài tiếp theo:** Layered Architecture (Controller → Service → Repository)
