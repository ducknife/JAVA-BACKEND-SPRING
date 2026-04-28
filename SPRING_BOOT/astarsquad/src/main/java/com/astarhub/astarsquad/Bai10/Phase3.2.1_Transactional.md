Chấp nhận phản hồi của bạn. Xin lỗi vì cách giải thích trước chưa đủ "đô" với bạn. Chúng ta sẽ "mổ xẻ" nó ở cấp độ **Code & JVM (Java Virtual Machine)** để xem thực sự Spring làm cái trò gì ở bên dưới.

Đây là bản **Deep Dive** về cơ chế Proxy của `@Transactional`.

---

## 📑 Mục Lục

- [1. Spring tạo ra cái gì lúc Runtime? (The Generated Code)](#1-spring-tạo-ra-cái-gì-lúc-runtime-the-generated-code)
- [2. Tại sao "Self-Invocation" lại chết? (Technical Explanation)](#2-tại-sao-self-invocation-lại-chết-technical-explanation)
- [3. Đáp án cho bài tập tình huống trước](#3-đáp-án-cho-bài-tập-tình-huống-trước)
- [4. Giải pháp "Hardcore" (Nếu bắt buộc phải dùng Self-Invocation)](#4-giải-pháp-hardcore-nếu-bắt-buộc-phải-dùng-self-invocation)
  - [Cách 1: Self-Injection (Tiêm chính mình)](#cách-1-self-injection-tiêm-chính-mình)
  - [Cách 2: AopContext (Hàng "cấm" - nhưng mạnh)](#cách-2-aopcontext-hàng-cấm-nhưng-mạnh)

---

# 📖 Module 3.2 (Phần A - Deep Dive): Giải phẫu Proxy & Transaction Interceptor

Bạn đừng nhìn `@Transactional` như một annotation vô tri. Hãy nhìn nó như một **công thức để sinh code**.

## 1. Spring tạo ra cái gì lúc Runtime? (The Generated Code)

Khi Spring khởi động (Application Context startup), nó quét thấy class `OrderService` có `@Transactional`. Nó **không** dùng class `OrderService` của bạn ngay.

Nó sử dụng thư viện **CGLIB** (Code Generation Library) để âm thầm tạo ra một class con (Subclass) kế thừa từ class của bạn ngay trong bộ nhớ.

Đây là đoạn code giả lập (Pseudo-code) mô tả chính xác những gì class Proxy đó thực hiện:

```java
// ĐÂY LÀ CODE SPRING TỰ SINH RA (Bạn không nhìn thấy)
public class OrderService$$EnhancerBySpringCGLIB extends OrderService {

    // 1. Giữ tham chiếu đến đối tượng gốc của bạn (Target Bean)
    private final OrderService target;
    private final PlatformTransactionManager transactionManager;

    // Override lại method của bạn để chèn logic Transaction vào
    @Override
    public void createOrder(OrderDTO dto) {
        TransactionStatus status = null;
        try {
            // --- BƯỚC 1: MỞ GIAO DỊCH (AOP BEFORE) ---
            // Tương đương: connection.setAutoCommit(false)
            status = transactionManager.getTransaction(new DefaultTransactionDefinition());
            
            // --- BƯỚC 2: GỌI HÀM THẬT CỦA BẠN ---
            // Gọi vào method của object gốc (Target)
            target.createOrder(dto); 

            // --- BƯỚC 3: COMMIT (AOP AFTER RETURNING) ---
            // Tương đương: connection.commit()
            transactionManager.commit(status);
            
        } catch (RuntimeException | Error ex) {
            // --- BƯỚC 4: ROLLBACK (AOP AFTER THROWING) ---
            // Tương đương: connection.rollback()
            if (status != null) {
                transactionManager.rollback(status);
            }
            throw ex; // Ném lỗi ra tiếp cho Controller biết
        }
    }
}

```

**Nhìn vào code trên, bạn sẽ thấy:**

* Logic Transaction **BAO BỌC** (Wrap) logic nghiệp vụ của bạn.
* Nếu bạn gọi từ ngoài vào (`Controller -> Proxy -> Target`), nó sẽ chạy qua các dòng `try-catch` và `transactionManager`.

---

## 2. Tại sao "Self-Invocation" lại chết? (Technical Explanation)

Quay lại vấn đề: Tại sao hàm A gọi hàm B (`this.methodB()`) thì Transaction của B bị chết?

Hãy nhìn vào từ khóa **`this`** trong Java.

```java
// Class gốc của bạn
public class OrderService {

    public void generateFullReport() {
        // ... logic A ...
        
        // Dòng này khi biên dịch ra bytecode thực chất là: this.createDailyReport();
        // 'this' ở đây là ai? 
        // Là instance của chính class OrderService (Target Object), KHÔNG PHẢI là Proxy Object.
        createDailyReport(); 
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createDailyReport() {
        // ...
    }
}

```

**Phân tích luồng chạy:**

1. Khi code chạy bên trong `generateFullReport` (thuộc Target Object), bạn đang đứng ở **bên trong** vòng bảo vệ của Proxy rồi.
2. Khi gọi `this.createDailyReport()`, JVM sẽ tìm hàm `createDailyReport` ngay trong object hiện tại (`this`) để chạy.
3. Nó **không nhảy ngược ra ngoài** để đi qua lớp vỏ Proxy (nơi chứa đoạn code `transactionManager.getTransaction` ở phần 1).
4. => Hàm `createDailyReport` chạy như một hàm Java thuần túy, annotation `@Transactional` hoàn toàn bị bỏ qua.

> **Ví dụ đời sống:** Bạn đi qua cổng an ninh (Proxy) để vào tòa nhà (Target). Khi bạn đã ở trong tòa nhà rồi, bạn đi từ phòng này sang phòng khác (Self-invocation), bạn không cần (và không thể) đi qua cổng an ninh lần nữa.

---

## 3. Đáp án cho bài tập tình huống trước

**Câu hỏi:**
`generateFullReport` (Có Tx) gọi `createDailyReport` (Có Tx + `REQUIRES_NEW`).

**Đáp án chính xác:**

* Hàm `createDailyReport` **VẪN CHẠY**, nhưng cấu hình `REQUIRES_NEW` **VÔ TÁC DỤNG**.
* Nó **KHÔNG** tạo ra Transaction mới.
* Nó chạy chung trong Transaction của `generateFullReport` (do tính chất lan truyền mặc định, hoặc đơn giản là nó đang chạy trong dòng chảy của hàm cha).

**Hậu quả:**
Nếu `createDailyReport` bị lỗi và Rollback -> Nó làm Rollback luôn cả `generateFullReport`. (Trong khi ý đồ của `REQUIRES_NEW` thường là: Con chết kệ con, Cha vẫn sống).

---

## 4. Giải pháp "Hardcore" (Nếu bắt buộc phải dùng Self-Invocation)

Nếu bạn không muốn tách Service (cách Clean nhất), bạn phải dùng các kỹ thuật can thiệp vào Spring Context.

### Cách 1: Self-Injection (Tiêm chính mình)

Bạn ép Spring tiêm cái **Proxy** của chính class này vào trong nó.

```java
@Service
public class ReportService {

    @Autowired
    @Lazy // Bắt buộc dùng Lazy để tránh lỗi vòng lặp: Đang tạo A cần A
    private ReportService selfProxy; // Đây là Proxy, không phải 'this'

    public void generateFullReport() {
        // Thay vì gọi createDailyReport(), hãy gọi:
        selfProxy.createDailyReport(); 
        // -> Lúc này nó đi qua Proxy -> Transaction mới được tạo ra!
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createDailyReport() { ... }
}

```

### Cách 2: AopContext (Hàng "cấm" - nhưng mạnh)

Dùng class tiện ích của Spring AOP để móc lấy Proxy hiện tại ra. (Yêu cầu phải bật config `exposeProxy = true`).

```java
public void generateFullReport() {
    // Móc lấy Proxy từ ThreadLocal hiện tại
    ((ReportService) AopContext.currentProxy()).createDailyReport();
}

```

---

Phần giải thích này đã đủ chi tiết về mặt kỹ thuật (Proxy code & `this` pointer) chưa bạn?
Nếu đã thông suốt phần **Cơ chế (Part A)**, chúng ta sẽ sang **Phần B: Propagation** - nơi chúng ta học cách điều khiển các Transaction lồng nhau một cách chủ động.
