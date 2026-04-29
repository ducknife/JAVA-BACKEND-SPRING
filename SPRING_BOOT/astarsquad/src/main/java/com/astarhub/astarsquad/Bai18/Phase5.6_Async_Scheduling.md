# ⏰ Phase 5.6: Async & Scheduling

---

## 📑 Mục Lục

- [1. @Async — Chạy Bất Đồng Bộ](#1-async--chạy-bất-đồng-bộ)
- [2. @Scheduled — Lập Lịch Tự Động](#2-scheduled--lập-lịch-tự-động)
- [3. Xử Lý Exception Trong Async](#3-xử-lý-exception-trong-async)
- [✅ Checklist](#-checklist)

---

## 1. @Async — Chạy Bất Đồng Bộ

```
Đồng bộ (mặc định):
saveOrder() → sendEmail() (2s) → sendSMS() (1s) → Response (3s total)

Bất đồng bộ (@Async):
saveOrder() → sendEmail() (chạy background)
             → sendSMS()  (chạy background)
             → Response ngay! (< 100ms)
```

### 1.1 Setup

```java
@Configuration
@EnableAsync  // Kích hoạt @Async
public class AsyncConfig {

    @Bean
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);      // Threads mặc định
        executor.setMaxPoolSize(10);      // Threads tối đa
        executor.setQueueCapacity(50);    // Queue chờ
        executor.setThreadNamePrefix("async-");
        executor.initialize();
        return executor;
    }
}
```

### 1.2 Sử Dụng

```java
@Service
@RequiredArgsConstructor
public class NotificationService {

    /**
     * @Async: Method chạy trên thread riêng.
     * Caller KHÔNG CHỜ method hoàn thành.
     * 
     * ⚠️ QUAN TRỌNG:
     * - @Async chỉ hoạt động khi gọi TỪ BÊN NGOÀI class
     * - Gọi this.sendEmail() (cùng class) → @Async BỊ BỎ QUA
     * - Vì Spring AOP proxy chỉ intercept call qua proxy
     */
    @Async
    public void sendEmail(String to, String subject, String body) {
        // Chạy trên thread riêng — không block caller
        emailSender.send(to, subject, body);
    }

    // Return Future nếu caller cần kết quả
    @Async
    public CompletableFuture<Boolean> sendEmailWithResult(String to) {
        boolean success = emailSender.send(to, "Subject", "Body");
        return CompletableFuture.completedFuture(success);
    }
}

// Controller — gọi async method
@PostMapping("/orders")
public ResponseEntity<?> createOrder(@Valid @RequestBody CreateOrderRequest req) {
    OrderResponse order = orderService.createOrder(req);

    // Gửi email background — KHÔNG CHỜ
    notificationService.sendEmail(order.email(), "Order Confirmed", "...");

    return ResponseEntity.status(HttpStatus.CREATED).body(order);
    // Response trả ngay, email gửi background
}
```

### 1.3 Chờ Kết Quả Async

```java
@Async
public CompletableFuture<Report> generateReport(Long userId) {
    // Tính toán nặng — chạy background
    Report report = heavyComputation(userId);
    return CompletableFuture.completedFuture(report);
}

// Caller chờ kết quả
CompletableFuture<Report> future = reportService.generateReport(userId);
Report report = future.get(30, TimeUnit.SECONDS);  // chờ tối đa 30s

// Hoặc combine nhiều async
CompletableFuture<Report> report1 = reportService.generateReport(1L);
CompletableFuture<Report> report2 = reportService.generateReport(2L);
CompletableFuture.allOf(report1, report2).join();  // chờ cả 2
```

---

## 2. @Scheduled — Lập Lịch Tự Động

### 2.1 Setup

```java
@Configuration
@EnableScheduling  // Kích hoạt @Scheduled
public class SchedulingConfig {}
```

### 2.2 Các Loại Schedule

```java
@Component
@RequiredArgsConstructor
public class ScheduledTasks {

    private final TokenBlacklistService tokenService;
    private final ReportService reportService;

    /**
     * fixedRate: Chạy mỗi 60 giây (tính từ lúc BẮT ĐẦU lần trước)
     * Lần trước chưa xong vẫn chạy lần sau (có thể overlap!)
     */
    @Scheduled(fixedRate = 60_000)  // 60 giây
    public void cleanExpiredTokens() {
        tokenService.removeExpiredTokens();
    }

    /**
     * fixedDelay: Chạy 30 giây SAU KHI lần trước HOÀN THÀNH
     * Đảm bảo KHÔNG overlap
     */
    @Scheduled(fixedDelay = 30_000)
    public void syncData() {
        externalService.sync();
    }

    /**
     * cron: Cron expression — linh hoạt nhất
     * Format: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "0 0 2 * * *")  // 2:00 AM mỗi ngày
    public void generateDailyReport() {
        reportService.generateDaily();
    }

    @Scheduled(cron = "0 0 9 * * MON")  // 9:00 AM mỗi thứ Hai
    public void sendWeeklyDigest() {
        emailService.sendWeeklyDigest();
    }

    @Scheduled(cron = "0 */5 * * * *")  // Mỗi 5 phút
    public void healthCheck() {
        monitoringService.check();
    }
}
```

### 2.3 Cron Expression Cheat Sheet

```
┌──────── second (0-59)
│ ┌────── minute (0-59)
│ │ ┌──── hour (0-23)
│ │ │ ┌── day of month (1-31)
│ │ │ │ ┌ month (1-12)
│ │ │ │ │ ┌ day of week (0-7, 0=Sun, MON-SUN)
│ │ │ │ │ │
* * * * * *

0 0 2 * * *     = 2:00 AM mỗi ngày
0 0 */2 * * *   = Mỗi 2 giờ
0 30 9 * * MON  = 9:30 sáng thứ Hai
0 0 0 1 * *     = Đầu mỗi tháng
*/10 * * * * *   = Mỗi 10 giây
```

---

## 3. Xử Lý Exception Trong Async

```java
@Configuration
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (throwable, method, params) -> {
            log.error("Async error in {}: {}", method.getName(), throwable.getMessage());
            // Gửi alert, log vào monitoring...
        };
    }
}
```

---

## ✅ Checklist

- [ ] @EnableAsync + @Async cho background tasks
- [ ] Hiểu @Async chỉ hoạt động khi gọi từ bên ngoài class (AOP proxy)
- [ ] CompletableFuture cho async có return value
- [ ] @EnableScheduling + @Scheduled
- [ ] fixedRate vs fixedDelay vs cron
- [ ] Cron expression cheat sheet

---

> **Phase 5 hoàn thành!** Tiếp theo: Phase 6 (Microservices & DevOps)
