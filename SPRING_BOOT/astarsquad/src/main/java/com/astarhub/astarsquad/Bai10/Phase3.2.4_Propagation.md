Tuyệt vời. Chúng ta đã "mổ xẻ" xong cơ chế Proxy. Bây giờ, khi đã hiểu ai là người mở/đóng Transaction, chúng ta sẽ trả lời câu hỏi: **"Chuyện gì xảy ra khi Service A gọi Service B?"**

Chào mừng bạn đến với **Phần B: Transaction Propagation (Sự lan truyền)**.

Đây là phần kiến thức giúp bạn thiết kế những luồng nghiệp vụ phức tạp (như đặt hàng kèm thanh toán, ghi log hệ thống) mà không bị lỗi dữ liệu chéo.

---

## 📑 Mục Lục

- [1. Propagation là gì?](#1-propagation-là-gì)
- [2. Các loại Propagation phổ biến (Cần thuộc lòng)](#2-các-loại-propagation-phổ-biến-cần-thuộc-lòng)
  - [2.1. `REQUIRED` (Mặc định - "Người đồng đội")](#21-required-mặc-định-người-đồng-đội)
  - [2.2. `REQUIRES_NEW` (Cô lập - "Người hướng nội")](#22-requires_new-cô-lập-người-hướng-nội)
  - [2.3. `MANDATORY` (Bắt buộc - "Kẻ ăn bám")](#23-mandatory-bắt-buộc-kẻ-ăn-bám)
- [3. Case Study: Hệ thống Đặt hàng & Ghi Log](#3-case-study-hệ-thống-đặt-hàng-ghi-log)
  - [Kịch bản:](#kịch-bản)
  - [Cấu hình 1: Dùng Mặc định (`REQUIRED`) - Sai lầm thường gặp](#cấu-hình-1-dùng-mặc-định-required-sai-lầm-thường-gặp)
  - [Cấu hình 2: Dùng `REQUIRES_NEW` - Giải pháp chuẩn](#cấu-hình-2-dùng-requires_new-giải-pháp-chuẩn)
- [4. Bảng tổng sắp (Cheat Sheet)](#4-bảng-tổng-sắp-cheat-sheet)
- [🧠 Bài tập tình huống (Phần B)](#bài-tập-tình-huống-phần-b)

---

# 📖 Module 3.2 (Phần B): Transaction Propagation

## 1. Propagation là gì?

**Propagation** (Sự lan truyền) định nghĩa hành vi của Transaction khi một method (có `@Transactional`) được gọi từ một method khác (cũng có hoặc không có `@Transactional`).

Nó trả lời câu hỏi: *"Tôi nên tham gia vào Transaction đang có sẵn, hay tôi nên tự tạo một cái mới riêng cho mình?"*

---

## 2. Các loại Propagation phổ biến (Cần thuộc lòng)

Spring cung cấp 7 loại, nhưng thực tế đi làm bạn chỉ cần nắm vững 3 loại cốt lõi sau đây (chiếm 99% trường hợp):

### 2.1. `REQUIRED` (Mặc định - "Người đồng đội")

Đây là cấu hình mặc định nếu bạn không viết gì thêm.

* **Quy tắc:**
* Nếu bên ngoài **đã có** Transaction -> Tôi xin **dùng chung** (Join).
* Nếu bên ngoài **chưa có** -> Tôi tự **tạo mới** (Create New).


* **Hệ quả:** Vì dùng chung, nên "Sống cùng sống, chết cùng chết". Nếu Service con bị Rollback, Service cha cũng bị đánh dấu Rollback theo (kể cả khi Service cha đã try-catch lỗi).

### 2.2. `REQUIRES_NEW` (Cô lập - "Người hướng nội")

Dùng khi bạn muốn đoạn code này chạy hoàn toàn độc lập.

* **Quy tắc:**
* Luôn luôn **TẠO MỚI** một Transaction riêng biệt.
* Nếu bên ngoài đang có Transaction, nó sẽ **TẠM DỪNG** (Suspend) cái cũ lại, chạy xong cái mới rồi mới khôi phục cái cũ.


* **Hệ quả:** "Việc ai người nấy làm". Service con chết (Rollback) không ảnh hưởng Service cha. Service cha chết không ảnh hưởng Service con (nếu con đã commit xong).
* **Ứng dụng:** Ghi Log audit, gửi thông báo (những thứ dù nghiệp vụ chính thất bại thì vẫn phải lưu lại dấu vết).

### 2.3. `MANDATORY` (Bắt buộc - "Kẻ ăn bám")

* **Quy tắc:** Bắt buộc phải có Transaction từ bên ngoài truyền vào. Nếu không có -> Ném lỗi `TransactionRequiredException`.
* **Ứng dụng:** Dùng cho các hàm helper/repository chỉ được phép chạy trong một ngữ cảnh nghiệp vụ lớn hơn, không được chạy lẻ loi.

---

## 3. Case Study: Hệ thống Đặt hàng & Ghi Log

Chúng ta sẽ dùng code (Constructor Injection) để minh họa sự khác biệt sống còn giữa `REQUIRED` và `REQUIRES_NEW`.

### Kịch bản:

1. **OrderService**: Lưu đơn hàng.
2. **AuditService**: Lưu lịch sử ("User A vừa đặt hàng").
3. **Tình huống:** Lưu đơn hàng thành công, nhưng lưu Log bị lỗi. Hoặc lưu Order lỗi, nhưng vẫn muốn lưu Log lỗi.

### Cấu hình 1: Dùng Mặc định (`REQUIRED`) - Sai lầm thường gặp

```java
// Service Cha
@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final AuditService auditService;

    @Transactional // (1) Mở Tx A
    public void createOrder(Order order) {
        orderRepository.save(order);
        
        try {
            // Gọi Service con
            auditService.logAction("Tạo đơn hàng " + order.getId());
        } catch (Exception e) {
            // Cố tình Try-Catch để cứu Order nếu Log lỗi
            System.out.println("Log lỗi nhưng kệ nó");
        }
    }
}

// Service Con
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditRepository auditRepository;

    @Transactional // (2) Mặc định là REQUIRED -> Join vào Tx A
    public void logAction(String msg) {
        auditRepository.save(new AuditLog(msg));
        throw new RuntimeException("Lỗi ghi log!"); // Giả lập lỗi
    }
}

```

**Kết quả đau thương:**

* Mặc dù `OrderService` đã `try-catch` lỗi của `AuditService`.
* NHƯNG, do dùng chung Transaction A. Khi `AuditService` ném lỗi -> Transaction A bị đánh dấu là **"Rollback-Only"** ở mức Database connection.
* Khi `createOrder` chạy xong và định Commit -> Spring phát hiện cờ Rollback-Only -> Nó Rollback tất cả.
* => **Mất luôn cả Order**.

---

### Cấu hình 2: Dùng `REQUIRES_NEW` - Giải pháp chuẩn

Chúng ta chỉ sửa đúng 1 dòng ở Service con:

```java
@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditRepository auditRepository;

    // TẠO TRANSACTION B RIÊNG BIỆT
    @Transactional(propagation = Propagation.REQUIRES_NEW) 
    public void logAction(String msg) {
        auditRepository.save(new AuditLog(msg));
        // Nếu lỗi ở đây -> Chỉ Rollback Tx B. Tx A của Order vẫn an toàn.
    }
}

```

**Kết quả:**

* `OrderService` lưu Order (Tx A đang chạy).
* Gọi `AuditService` -> Tx A tạm dừng -> Tx B được tạo và chạy.
* Nếu Tx B lỗi -> Rollback B (Mất log). Ném exception ra ngoài.
* `OrderService` bắt exception (Try-Catch) -> Tx A tiếp tục chạy -> Commit.
* => **Order vẫn được lưu thành công.**

---

## 4. Bảng tổng sắp (Cheat Sheet)

| Loại Propagation | Có Tx bên ngoài | Không có Tx bên ngoài | Hành vi |
| --- | --- | --- | --- |
| **REQUIRED** (Default) | Join (Dùng chung) | Create New | Đồng sinh đồng tử. |
| **REQUIRES_NEW** | Suspend (Tạm dừng cũ) | Create New | Độc lập hoàn toàn. |
| **MANDATORY** | Join | **Throw Exception** | Phải có người bao nuôi. |
| **SUPPORTS** | Join | Run non-transactional | Có thì vui, không có cũng được. |
| **NOT_SUPPORTED** | Suspend | Run non-transactional | Ghét Transaction (thường dùng cho các hàm chỉ đọc file, gửi mail nặng). |

---

## 🧠 Bài tập tình huống (Phần B)

Để chốt hạ phần này, mời bạn giải bài toán sau:

Bạn có 3 Service: A, B, C.

* A gọi B.
* B gọi C.

Cấu hình như sau:

* A: `@Transactional` (REQUIRED)
* B: `@Transactional(propagation = REQUIRED)`
* C: `@Transactional(propagation = REQUIRES_NEW)`

**Kịch bản:**

1. A lưu dữ liệu thành công.
2. B lưu dữ liệu thành công.
3. C bị lỗi `RuntimeException` và Rollback.
4. B không try-catch lỗi của C. A cũng không try-catch lỗi từ B.

**Câu hỏi:**
Dữ liệu của A và B có được lưu vào Database không? Tại sao?

(Gợi ý: Hãy vẽ sơ đồ xem ai chung hội với ai, ai đứng riêng).

Mời bạn trổ tài! 👇
