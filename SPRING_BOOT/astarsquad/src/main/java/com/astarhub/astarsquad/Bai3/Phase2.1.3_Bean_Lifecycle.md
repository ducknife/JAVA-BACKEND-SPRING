# 📚 Bài 3: Bean Lifecycle

---

## 🎯 Mục tiêu
Hiểu **vòng đời của Bean** và cách can thiệp vào các giai đoạn.

---

## 1. Vòng đời Bean là gì?

> **Lifecycle = Các giai đoạn từ lúc Bean được tạo đến lúc bị hủy**

```
┌─────────────────────────────────────────────────────────────┐
│                    BEAN LIFECYCLE                            │
└─────────────────────────────────────────────────────────────┘

    ┌──────────────┐
    │  App Start   │
    └──────┬───────┘
           ▼
    ┌──────────────┐
    │ 1. Tạo Bean  │  ← Spring gọi constructor
    └──────┬───────┘
           ▼
    ┌──────────────┐
    │ 2. Inject    │  ← Spring inject dependencies
    │ Dependencies │
    └──────┬───────┘
           ▼
    ┌──────────────┐
    │ 3. Init      │  ← @PostConstruct chạy ở đây
    └──────┬───────┘
           ▼
    ┌──────────────┐
    │ 4. Ready     │  ← Bean sẵn sàng sử dụng
    │   to use     │
    └──────┬───────┘
           ▼
    ┌──────────────┐
    │ 5. Destroy   │  ← @PreDestroy chạy ở đây (khi app shutdown)
    └──────────────┘
```

---

## 2. @PostConstruct - Chạy sau khi tạo Bean

### Khi nào cần?

Khi bạn cần **khởi tạo gì đó** sau khi dependencies đã được inject.

```java
@Service
public class ProductService {
    
    private final ProductRepository productRepository;
    private List<String> cachedCategories;
    
    // 1. Constructor chạy trước
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
        // ⚠️ Chưa thể dùng productRepository ở đây vì logic phức tạp
    }
    
    // 2. @PostConstruct chạy sau khi inject xong
    @PostConstruct
    public void init() {
        System.out.println("🚀 ProductService đang khởi tạo...");
        
        // Giờ đã có productRepository, có thể dùng
        this.cachedCategories = productRepository.findAllCategories();
        
        System.out.println("✅ Đã load " + cachedCategories.size() + " categories");
    }
    
    public List<String> getCategories() {
        return cachedCategories;
    }
}
```

### Thứ tự chạy:

```
1. new ProductService(productRepository)  ← Constructor
2. productRepository được inject          ← Dependency Injection
3. init()                                 ← @PostConstruct
4. Bean sẵn sàng sử dụng
```

---

## 3. @PreDestroy - Chạy trước khi Bean bị hủy

### Khi nào cần?

Khi bạn cần **dọn dẹp resources** trước khi app tắt.

```java
@Service
public class ConnectionService {
    
    private Connection connection;
    
    @PostConstruct
    public void init() {
        System.out.println("🔌 Đang mở connection...");
        this.connection = openConnection();
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("🔌 Đang đóng connection...");
        if (connection != null) {
            connection.close();
        }
        System.out.println("✅ Đã đóng connection");
    }
    
    private Connection openConnection() {
        // Mở connection đến database/API
        return new Connection();
    }
}
```

### Khi nào @PreDestroy được gọi?

```
- App shutdown bình thường (Ctrl+C, stop server)
- Spring Context close
- Redeploy application

⚠️ KHÔNG được gọi khi:
- Kill process (kill -9)
- Crash đột ngột
- Prototype scope (Spring không quản lý destroy)
```

---

## 4. Ví dụ thực tế: Cache Service

```java
@Service
public class CacheService {
    
    private Map<String, Object> cache;
    private ScheduledExecutorService scheduler;
    
    @PostConstruct
    public void init() {
        System.out.println("🚀 Khởi tạo Cache Service...");
        
        // 1. Tạo cache
        this.cache = new ConcurrentHashMap<>();
        
        // 2. Tạo scheduler để cleanup cache định kỳ
        this.scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(
            this::cleanExpiredCache,
            1, 1, TimeUnit.HOURS
        );
        
        System.out.println("✅ Cache Service sẵn sàng!");
    }
    
    @PreDestroy
    public void cleanup() {
        System.out.println("🛑 Đang tắt Cache Service...");
        
        // 1. Tắt scheduler
        if (scheduler != null) {
            scheduler.shutdown();
        }
        
        // 2. Clear cache
        if (cache != null) {
            cache.clear();
        }
        
        System.out.println("✅ Cache Service đã tắt!");
    }
    
    public void put(String key, Object value) {
        cache.put(key, value);
    }
    
    public Object get(String key) {
        return cache.get(key);
    }
    
    private void cleanExpiredCache() {
        System.out.println("🧹 Đang dọn cache...");
        // Logic xóa cache hết hạn
    }
}
```

---

## 5. Ví dụ thực tế: Logging khi App start/stop

```java
@Component
public class AppLifecycleLogger {
    
    @PostConstruct
    public void onStart() {
        System.out.println("═══════════════════════════════════");
        System.out.println("🚀 Application Started!");
        System.out.println("   Time: " + LocalDateTime.now());
        System.out.println("═══════════════════════════════════");
    }
    
    @PreDestroy
    public void onStop() {
        System.out.println("═══════════════════════════════════");
        System.out.println("🛑 Application Stopping...");
        System.out.println("   Time: " + LocalDateTime.now());
        System.out.println("═══════════════════════════════════");
    }
}
```

**Output khi chạy app:**
```
═══════════════════════════════════
🚀 Application Started!
   Time: 2026-01-02T10:30:00
═══════════════════════════════════
```

**Output khi tắt app:**
```
═══════════════════════════════════
🛑 Application Stopping...
   Time: 2026-01-02T11:45:00
═══════════════════════════════════
```

---

## 6. Thứ tự thực thi nhiều Bean

```java
@Service
public class ServiceA {
    @PostConstruct
    public void init() {
        System.out.println("1. ServiceA init");
    }
    
    @PreDestroy
    public void destroy() {
        System.out.println("3. ServiceA destroy");  // Destroy ngược lại
    }
}

@Service
public class ServiceB {
    
    private final ServiceA serviceA;
    
    public ServiceB(ServiceA serviceA) {
        this.serviceA = serviceA;
    }
    
    @PostConstruct
    public void init() {
        System.out.println("2. ServiceB init");  // Sau ServiceA vì phụ thuộc
    }
    
    @PreDestroy
    public void destroy() {
        System.out.println("2. ServiceB destroy");  // Trước ServiceA
    }
}
```

**Output:**
```
App start:
1. ServiceA init
2. ServiceB init    ← ServiceB phụ thuộc ServiceA nên init sau

App shutdown:
2. ServiceB destroy ← Ngược lại: ServiceB destroy trước
3. ServiceA destroy
```

### Quy tắc:

```
Init:    Dependency trước → Dependent sau
Destroy: Dependent trước → Dependency sau (ngược lại)
```

---

## 7. @PostConstruct vs Constructor

### Khi nào dùng gì?

| | Constructor | @PostConstruct |
|---|-------------|----------------|
| **Gán field** | ✅ Dùng | ❌ Không cần |
| **Logic đơn giản** | ✅ Được | ✅ Được |
| **Cần dependency** | ❌ Chưa có | ✅ Đã có |
| **Gọi method phức tạp** | ⚠️ Tránh | ✅ Phù hợp |
| **Có thể throw Exception** | ⚠️ Hạn chế | ✅ Được |

### Ví dụ:

```java
@Service
public class ProductService {
    
    private final ProductRepository repo;
    private List<Product> featuredProducts;
    
    // Constructor: Chỉ gán field
    public ProductService(ProductRepository repo) {
        this.repo = repo;
    }
    
    // @PostConstruct: Logic phức tạp, cần dùng dependency
    @PostConstruct
    public void init() {
        this.featuredProducts = repo.findFeaturedProducts();
    }
}
```

---

## 8. Lưu ý quan trọng

### ⚠️ @PreDestroy không chạy với Prototype

```java
@Component
@Scope("prototype")
public class PrototypeBean {
    
    @PreDestroy  // ❌ KHÔNG BAO GIỜ ĐƯỢC GỌI!
    public void cleanup() {
        System.out.println("Cleanup...");
    }
}
```

**Lý do:** Spring không quản lý lifecycle của Prototype sau khi tạo.

### ⚠️ Đừng làm quá nhiều trong @PostConstruct

```java
// ❌ SAI - Quá nặng, block app start
@PostConstruct
public void init() {
    loadMillionsOfRecords();  // Chậm!
    callExternalAPI();         // Có thể timeout!
    warmupCache();             // Tốn thời gian!
}

// ✅ ĐÚNG - Nhẹ nhàng, hoặc dùng async
@PostConstruct
public void init() {
    // Chỉ setup cơ bản
    this.cache = new ConcurrentHashMap<>();
}

// Load data async sau khi app start
@EventListener(ApplicationReadyEvent.class)
public void onReady() {
    CompletableFuture.runAsync(this::loadData);
}
```

---

## 9. Cách khác: InitializingBean & DisposableBean

Spring cung cấp interface (ít dùng hơn):

```java
@Service
public class MyService implements InitializingBean, DisposableBean {
    
    @Override
    public void afterPropertiesSet() {  // Giống @PostConstruct
        System.out.println("Init...");
    }
    
    @Override
    public void destroy() {  // Giống @PreDestroy
        System.out.println("Destroy...");
    }
}
```

### So sánh:

| | @PostConstruct / @PreDestroy | Interface |
|---|------------------------------|-----------|
| **Phổ biến** | ✅ Rất phổ biến | ❌ Ít dùng |
| **Coupling** | Loose (annotation) | Tight (implement interface) |
| **Đọc code** | Dễ hiểu | Phải biết interface |

**→ Dùng @PostConstruct / @PreDestroy là chuẩn!**

---

## 📌 Tóm tắt

| Annotation | Khi nào chạy | Use case |
|------------|--------------|----------|
| **@PostConstruct** | Sau khi inject xong | Load cache, mở connection, init resources |
| **@PreDestroy** | Trước khi app tắt | Đóng connection, cleanup resources |

### Thứ tự:

```
Constructor → Inject → @PostConstruct → Ready → @PreDestroy → Destroy
```

### Quy tắc:

```
@PostConstruct = "Làm gì đó khi Bean sẵn sàng"
@PreDestroy    = "Dọn dẹp trước khi tắt"
```

---

**Bài tiếp theo:** ApplicationContext 
