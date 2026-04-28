Chào bạn, tiếp thu ý kiến của bạn. Đúng là ở phần trước mình tập trung nhiều vào *cơ chế hoạt động* (Proxy) mà chưa đi sâu vào *cách sử dụng* các thông số của chính annotation `@Transactional`.

Đây là bài **Deep Dive về Annotation @Transactional** - mổ xẻ tất cả các ngóc ngách, thông số và luật lệ của nó để bạn dùng cho chuẩn.

---

## 📑 Mục Lục

- [1. Vị trí đặt @Transactional (Scope)](#1-vị-trí-đặt-transactional-scope)
  - [1.1. Trên Method (Phổ biến nhất)](#11-trên-method-phổ-biến-nhất)
  - [1.2. Trên Class](#12-trên-class)
  - [1.3. Trên Interface (Không khuyên dùng)](#13-trên-interface-không-khuyên-dùng)
- [2. Các thuộc tính "Thần thánh" (Attributes)](#2-các-thuộc-tính-thần-thánh-attributes)
  - [2.1. `rollbackFor` & `noRollbackFor` (Quyết định sống chết)](#21-rollbackfor-norollbackfor-quyết-định-sống-chết)
  - [2.2. `readOnly` (Tối ưu hiệu năng)](#22-readonly-tối-ưu-hiệu-năng)
  - [2.3. `timeout` (Ngắt giao dịch treo)](#23-timeout-ngắt-giao-dịch-treo)
- [3. Những luật bất thành văn (Strict Rules)](#3-những-luật-bất-thành-văn-strict-rules)
  - [Luật 1: Chỉ áp dụng cho `public` method](#luật-1-chỉ-áp-dụng-cho-public-method)
  - [Luật 2: Cạm bẫy "Self-Invocation" (Tự gọi chính mình)](#luật-2-cạm-bẫy-self-invocation-tự-gọi-chính-mình)
- [4. Tổng kết: Template chuẩn cho Senior](#4-tổng-kết-template-chuẩn-cho-senior)

---

# 📖 Module 3.2 (Phần A - Bổ sung): Deep Dive về @Transactional

Annotation `@Transactional` không chỉ là một cái nhãn dán "cho có". Nó là một bảng điều khiển với nhiều nút vặn (attributes) để tinh chỉnh hành vi của giao dịch.

## 1. Vị trí đặt @Transactional (Scope)

Bạn có thể đặt nó ở 3 chỗ, và độ ưu tiên sẽ khác nhau:

### 1.1. Trên Method (Phổ biến nhất)

* **Tác dụng:** Chỉ áp dụng Transaction cho method đó.
* **Độ ưu tiên:** Cao nhất. Nó sẽ ghi đè (override) cấu hình ở cấp Class.

### 1.2. Trên Class

* **Tác dụng:** Áp dụng cho **TẤT CẢ** các method `public` có trong class đó.
* **Ví dụ:** Bạn đặt `@Transactional` trên class `UserService`, thì `createUser`, `updateUser` đều có transaction.

### 1.3. Trên Interface (Không khuyên dùng)

* **Lưu ý:** Spring không khuyến khích đặt trên Interface. Vì nếu bạn dùng CGLIB Proxy (mặc định của Spring Boot hiện nay), cấu hình trên Interface có thể bị lờ đi. **Luôn đặt trên Class hoặc Method.**

> **Quy tắc vàng:** Đặt `@Transactional(readOnly = true)` ở cấp Class (để mặc định là chỉ đọc cho an toàn), và ghi đè `@Transactional` (ghi/xóa) ở từng method cụ thể cần sửa đổi dữ liệu.

```java
@Service
@Transactional(readOnly = true) // 1. Mặc định toàn bộ class là Read-Only (Tối ưu hiệu năng)
public class ProductService {

    public List<Product> getAll() { 
        return repo.findAll(); // Hưởng readOnly=true từ Class
    }

    @Transactional // 2. Ghi đè: Method này cần ghi dữ liệu -> Mở Transaction chuẩn (Read-Write)
    public void createProduct(Product p) {
        repo.save(p);
    }
}

```

---

## 2. Các thuộc tính "Thần thánh" (Attributes)

Ngoài `propagation` và `isolation` đã học, đây là 3 thuộc tính còn lại bạn phải nắm vững:

### 2.1. `rollbackFor` & `noRollbackFor` (Quyết định sống chết)

Đây là phần hay gây lỗi nhất.

* **Mặc định:** Chỉ rollback khi gặp `RuntimeException` hoặc `Error`. **KHÔNG** rollback khi gặp `Checked Exception` (VD: `IOException`, `SQLException`).
* **`rollbackFor`:** Chỉ định thêm các Exception cần rollback.
* *Best Practice:* `@Transactional(rollbackFor = Exception.class)` -> Rollback với mọi loại lỗi.


* **`noRollbackFor`:** Chỉ định các Exception **cho phép bỏ qua** (vẫn Commit dù có lỗi này).
* *Ví dụ:* Gửi SMS xác nhận bị lỗi (`SmsException`), nhưng vẫn muốn lưu User vào DB.



```java
@Transactional(noRollbackFor = SmsException.class)
public void register(User u) {
    repo.save(u);
    smsService.send(u); // Nếu lỗi SmsException, Transaction vẫn Commit
}

```

### 2.2. `readOnly` (Tối ưu hiệu năng)

* **Giá trị:** `true` / `false` (mặc định).
* **Tác dụng:**
1. **Tắt Dirty Checking:** Hibernate sẽ không tốn công so sánh trạng thái object để tìm thay đổi -> Tiết kiệm RAM và CPU.
2. **Database Optimization:** Với một số DB (như MySQL slave), nó giúp định tuyến query sang server đọc (Replica), giảm tải cho server ghi (Master).


* **Lưu ý:** Tuyệt đối không gọi lệnh `save/delete` trong hàm `readOnly=true`.

### 2.3. `timeout` (Ngắt giao dịch treo)

* **Đơn vị:** Giây (Seconds).
* **Tác dụng:** Nếu transaction chạy quá lâu (VD: query chậm, deadlock, xử lý file nặng), nó sẽ tự động bị ngắt và Rollback để giải phóng tài nguyên cho Database.

```java
@Transactional(timeout = 10) // Quá 10 giây tự chết
public void processHeavyData() { ... }

```

---

## 3. Những luật bất thành văn (Strict Rules)

Để `@Transactional` hoạt động, bạn phải tuân thủ 2 luật lệ của cơ chế Proxy:

### Luật 1: Chỉ áp dụng cho `public` method

* Nếu bạn đặt `@Transactional` lên method `private` hoặc `protected`, Spring sẽ **lờ nó đi** mà không báo lỗi gì cả. Transaction sẽ không được tạo.
* *Lý do:* Proxy chỉ có thể override các method public của class cha.

### Luật 2: Cạm bẫy "Self-Invocation" (Tự gọi chính mình)

* Như đã giải thích ở phần Proxy: Nếu method A gọi method B trong **cùng một class**, `@Transactional` của method B sẽ bị vô hiệu hóa.

---

## 4. Tổng kết: Template chuẩn cho Senior

Dưới đây là mẫu code chuẩn mực kết hợp tất cả kiến thức trên:

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 1. Mặc định Class là Read-Only để tối ưu
public class OrderService {

    private final OrderRepository orderRepository;

    // 2. Hàm đọc dữ liệu: Không cần @Transactional nữa vì đã hưởng từ Class
    public Order getOrder(Long id) {
        return orderRepository.findById(id).orElseThrow();
    }

    // 3. Hàm ghi dữ liệu: Phải override lại để cho phép ghi (readOnly = false)
    // 4. Cấu hình rollback chặt chẽ và timeout để an toàn
    @Transactional(
        rollbackFor = Exception.class, 
        timeout = 5
    )
    public void createOrder(Order order) throws Exception {
        // Logic phức tạp...
        orderRepository.save(order);
    }
}

```

Bạn thấy phần giải thích chi tiết về `@Transactional` này đã đủ rõ ràng chưa? Nếu "thấm" rồi, chúng ta sẽ quay lại giải bài toán **Isolation Level** (Mua vé xem phim) đang dang dở nhé?
