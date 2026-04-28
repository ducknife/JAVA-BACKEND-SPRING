# 📚 Bài 4: ApplicationContext

---

## 🎯 Mục tiêu
Hiểu **ApplicationContext là gì** và vai trò của nó trong Spring.

---

## 1. ApplicationContext là gì?

> **ApplicationContext = "Container" chứa và quản lý tất cả Bean**

```
┌─────────────────────────────────────────────────────────────┐
│                    APPLICATION CONTEXT                       │
│                    (Spring Container)                        │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│   │ProductService│  │ OrderService │  │  UserService │      │
│   └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│   ┌──────────────┐  ┌──────────────┐  ┌──────────────┐      │
│   │ProductRepo   │  │  OrderRepo   │  │   UserRepo   │      │
│   └──────────────┘  └──────────────┘  └──────────────┘      │
│                                                              │
│   + Quản lý lifecycle (tạo, inject, destroy)                │
│   + Quản lý scope (singleton, prototype...)                 │
│   + Đọc configuration                                       │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

---

## 2. ApplicationContext làm gì?

| Chức năng | Mô tả |
|-----------|-------|
| **Chứa Bean** | Lưu trữ tất cả Bean đã đăng ký |
| **Tạo Bean** | Gọi constructor, tạo instance |
| **Inject Dependencies** | Tự động wiring dependencies |
| **Quản lý Lifecycle** | Gọi @PostConstruct, @PreDestroy |
| **Đọc Configuration** | Load properties, profiles |
| **Event Publishing** | Publish/subscribe events |

---

## 3. Khi nào ApplicationContext được tạo?

```java
@SpringBootApplication
public class MyApplication {
    public static void main(String[] args) {
        // ↓ ApplicationContext được tạo ở đây
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### Quá trình khởi động:

```
SpringApplication.run()
        │
        ▼
┌───────────────────────────┐
│ 1. Tạo ApplicationContext │
└───────────────────────────┘
        │
        ▼
┌───────────────────────────┐
│ 2. Scan tìm @Component,   │
│    @Service, @Repository  │
└───────────────────────────┘
        │
        ▼
┌───────────────────────────┐
│ 3. Tạo Bean, Inject DI    │
└───────────────────────────┘
        │
        ▼
┌───────────────────────────┐
│ 4. Gọi @PostConstruct     │
└───────────────────────────┘
        │
        ▼
┌───────────────────────────┐
│ 5. App sẵn sàng!          │
└───────────────────────────┘
```

---

## 4. Lấy Bean từ ApplicationContext

### Cách 1: Inject ApplicationContext (ít dùng)

```java
@Service
public class MyService {
    
    private final ApplicationContext context;
    
    public MyService(ApplicationContext context) {
        this.context = context;
    }
    
    public void doSomething() {
        // Lấy Bean thủ công
        ProductService productService = context.getBean(ProductService.class);
        productService.findAll();
    }
}
```

### Cách 2: Dùng Constructor Injection (khuyên dùng ✅)

```java
@Service
public class MyService {
    
    private final ProductService productService;
    
    // ✅ Spring tự lấy từ ApplicationContext và inject
    public MyService(ProductService productService) {
        this.productService = productService;
    }
    
    public void doSomething() {
        productService.findAll();
    }
}
```

### So sánh:

| | getBean() thủ công | Constructor Injection |
|---|-------------------|----------------------|
| **Code** | Dài, phức tạp | Gọn, rõ ràng |
| **Test** | Khó mock | Dễ mock |
| **Coupling** | Tight (phụ thuộc Spring) | Loose |
| **Khuyên dùng** | ❌ Tránh | ✅ Dùng |

---

## 5. Khi nào cần dùng ApplicationContext trực tiếp?

### Rất hiếm! Chỉ khi:

```java
// 1. Lấy Bean động theo tên/điều kiện
@Service
public class PaymentProcessor {
    
    private final ApplicationContext context;
    
    public void process(String paymentType) {
        // Lấy Bean theo tên động
        PaymentService service = context.getBean(paymentType + "PaymentService", PaymentService.class);
        service.pay();
    }
}

// 2. Lấy tất cả Bean cùng loại
@Service
public class NotificationService {
    
    private final ApplicationContext context;
    
    public void notifyAll(String message) {
        // Lấy tất cả Bean implement Notifier
        Map<String, Notifier> notifiers = context.getBeansOfType(Notifier.class);
        notifiers.values().forEach(n -> n.send(message));
    }
}
```

---

## 6. Các loại ApplicationContext

| Loại | Mô tả | Khi nào dùng |
|------|-------|--------------|
| **AnnotationConfigApplicationContext** | Đọc config từ annotation | Standalone app |
| **GenericWebApplicationContext** | Cho web app | Web app |
| **SpringApplication** | Spring Boot auto-config | **Spring Boot** (phổ biến nhất) |

### Spring Boot tự chọn cho bạn:

```java
// Spring Boot tự động chọn loại phù hợp
SpringApplication.run(MyApplication.class, args);

// Không cần quan tâm loại nào, Spring Boot lo!
```

---

## 7. BeanFactory vs ApplicationContext

```
BeanFactory (Interface cha)
     │
     └── ApplicationContext (Interface con, mở rộng thêm)
```

| | BeanFactory | ApplicationContext |
|---|-------------|-------------------|
| **Tính năng** | Cơ bản | Đầy đủ |
| **Event** | ❌ | ✅ |
| **i18n** | ❌ | ✅ |
| **Bean loading** | Lazy | Eager (default) |
| **Dùng** | Hiếm | **Luôn dùng** |

**→ Luôn dùng ApplicationContext, quên BeanFactory đi!**

---

## 8. Application Events

ApplicationContext hỗ trợ publish/subscribe events:

```java
// 1. Tạo custom event
public class OrderCreatedEvent {
    private final Order order;
    
    public OrderCreatedEvent(Order order) {
        this.order = order;
    }
    
    public Order getOrder() {
        return order;
    }
}

// 2. Publish event
@Service
public class OrderService {
    
    private final ApplicationEventPublisher eventPublisher;
    
    public OrderService(ApplicationEventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
    
    public void createOrder(Order order) {
        // Save order...
        
        // Publish event
        eventPublisher.publishEvent(new OrderCreatedEvent(order));
    }
}

// 3. Listen event
@Component
public class EmailNotificationListener {
    
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("Gửi email cho order: " + event.getOrder().getId());
    }
}

@Component
public class InventoryListener {
    
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        System.out.println("Trừ kho cho order: " + event.getOrder().getId());
    }
}
```

### Luồng chạy:

```
OrderService.createOrder()
        │
        ▼ publishEvent(OrderCreatedEvent)
        │
        ├──► EmailNotificationListener.handleOrderCreated()
        │
        └──► InventoryListener.handleOrderCreated()
```

---

## 9. Built-in Events

Spring cung cấp sẵn một số events:

```java
@Component
public class AppEventListener {
    
    @EventListener
    public void onStart(ApplicationReadyEvent event) {
        System.out.println("🚀 App đã sẵn sàng!");
    }
    
    @EventListener
    public void onClose(ContextClosedEvent event) {
        System.out.println("🛑 App đang tắt...");
    }
}
```

| Event | Khi nào |
|-------|---------|
| **ContextRefreshedEvent** | Context được refresh |
| **ApplicationReadyEvent** | App hoàn toàn sẵn sàng |
| **ContextClosedEvent** | Context đang đóng |
| **ContextStartedEvent** | Context được start |
| **ContextStoppedEvent** | Context được stop |

---

## 10. Ví dụ thực tế

```java
@SpringBootApplication
public class MyApplication {
    
    public static void main(String[] args) {
        // Lấy ApplicationContext
        ApplicationContext context = SpringApplication.run(MyApplication.class, args);
        
        // Xem có bao nhiêu Bean
        System.out.println("Số Bean: " + context.getBeanDefinitionCount());
        
        // Liệt kê tên các Bean
        String[] beanNames = context.getBeanDefinitionNames();
        for (String name : beanNames) {
            System.out.println("- " + name);
        }
    }
}
```

---

## 📌 Tóm tắt

| Khái niệm | Ý nghĩa |
|-----------|---------|
| **ApplicationContext** | Container chứa và quản lý tất cả Bean |
| **getBean()** | Lấy Bean thủ công (tránh dùng) |
| **Constructor Injection** | Để Spring tự inject (khuyên dùng) |
| **ApplicationEventPublisher** | Publish events |
| **@EventListener** | Listen events |

### Quy tắc:

```
ApplicationContext = Hiểu để biết Spring hoạt động
Constructor Injection = Cách dùng hàng ngày
getBean() = Chỉ dùng khi thực sự cần (hiếm)
```

---

**Bài tiếp theo:** @Component, @Service, @Repository, @Controller
