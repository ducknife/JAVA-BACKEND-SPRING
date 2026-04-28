# 📚 Bài 1: IoC & Dependency Injection

---

## 🎯 Mục tiêu
Hiểu được **tại sao** cần IoC/DI và **cách hoạt động** trong Spring.

---

## 1. Vấn đề khi KHÔNG có DI

```java
public class OrderService {
    // ❌ Tự tạo dependency
    private EmailService emailService = new EmailService();
    private SmsService smsService = new SmsService();
    
    public void createOrder(Order order) {
        // xử lý...
        emailService.send("Order created");
    }
}
```

**Vấn đề:**
- `OrderService` **biết quá nhiều** về cách tạo `EmailService`
- Muốn đổi sang `MockEmailService` để test → phải sửa code
- Muốn đổi sang `SendGridService` → phải sửa code
- Khó mở rộng, khó bảo trì

---

## 2. Giải pháp: Dependency Injection

**Nguyên tắc:** Class **không tự tạo** dependency, mà **nhận từ bên ngoài**.

```java
@Service
public class OrderService {
    // ✅ Không tự tạo, chờ được inject
    private final EmailService emailService;
    
    // Constructor: Spring sẽ truyền EmailService vào
    public OrderService(EmailService emailService) {
        this.emailService = emailService;
    }
    
    public void createOrder(Order order) {
        emailService.send("Order created");
    }
}
```

**Lợi ích:**
- `OrderService` chỉ cần **interface**, không quan tâm implementation
- Dễ test: truyền `MockEmailService` qua constructor
- Dễ thay đổi: đổi implementation không cần sửa `OrderService`

---

## 3. Spring Container làm gì?

Khi ứng dụng start, Spring:

```
1. Scan tìm các class có @Component, @Service, @Repository...
   └── "À, có OrderService và EmailService cần quản lý"

2. Phân tích dependency
   └── "OrderService cần EmailService trong constructor"

3. Tạo theo thứ tự
   └── Tạo EmailService trước
   └── Tạo OrderService, truyền EmailService vào

4. Lưu vào Container
   └── Sẵn sàng sử dụng
```

---

## 4. Ví dụ thực hành

### File 1: NotificationService.java
```java
package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service  // ← Đánh dấu: "Spring ơi, hãy quản lý class này"
public class NotificationService {
    
    public void send(String message) {
        System.out.println("📧 Gửi: " + message);
    }
}
```

### File 2: OrderService.java
```java
package com.example.demo.service;

import org.springframework.stereotype.Service;

@Service
public class OrderService {
    
    private final NotificationService notificationService;
    
    // Spring thấy constructor cần NotificationService
    // → Tự động tìm và truyền vào
    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
    
    public void createOrder(String product) {
        System.out.println("📦 Tạo đơn: " + product);
        notificationService.send("Đơn hàng " + product + " đã tạo!");
    }
}
```

### File 3: DemoRunner.java (để test)
```java
package com.example.demo;

import com.example.demo.service.OrderService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DemoRunner implements CommandLineRunner {
    
    private final OrderService orderService;
    
    public DemoRunner(OrderService orderService) {
        this.orderService = orderService;
    }
    
    @Override
    public void run(String... args) {
        orderService.createOrder("iPhone 15");
    }
}
```

### Kết quả khi chạy:
```
📦 Tạo đơn: iPhone 15
📧 Gửi: Đơn hàng iPhone 15 đã tạo!
```

---

## 5. 3 cách Inject Dependency

### Cách 1: Constructor Injection ✅ (Khuyên dùng)
```java
@Service
public class OrderService {
    private final NotificationService notificationService;
    
    public OrderService(NotificationService notificationService) {
        this.notificationService = notificationService;
    }
}
```
- Dùng `final` → không thể thay đổi sau khi tạo
- Rõ ràng: nhìn constructor biết cần gì
- Dễ test: truyền mock qua constructor

### Cách 2: Field Injection ⚠️ (Không khuyến khích)
```java
@Service
public class OrderService {
    @Autowired
    private NotificationService notificationService;
}
```
- Ngắn gọn nhưng:
  - Không thể dùng `final`
  - Khó test (phải dùng reflection)
  - Dependencies bị ẩn

### Cách 3: Setter Injection
```java
@Service
public class OrderService {
    private NotificationService notificationService;
    
    @Autowired
    public void setNotificationService(NotificationService ns) {
        this.notificationService = ns;
    }
}
```
- Dùng khi dependency là optional

---

## 📌 Tóm tắt

| Khái niệm | Ý nghĩa |
|-----------|---------|
| **IoC** | Framework kiểm soát việc tạo object, không phải bạn |
| **DI** | Object nhận dependency từ bên ngoài, không tự tạo |
| **@Service** | Đánh dấu class để Spring quản lý |
| **Constructor Injection** | Cách inject được khuyên dùng |

---

**Bài tiếp theo:** Bean & Bean Lifecycle
