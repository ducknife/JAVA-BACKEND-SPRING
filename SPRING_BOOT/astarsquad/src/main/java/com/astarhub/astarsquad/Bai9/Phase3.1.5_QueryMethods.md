Rất tiếc, câu trả lời này **chưa chính xác** 100% rồi! 😅

Bạn hãy để ý kỹ lại class `Product` trong đề bài nhé:

```java
@Entity
public class Product {
    @Id
    private String productCode; // <-- Khóa chính là kiểu STRING
    ...
}

```

Do đó, tham số thứ 2 trong `JpaRepository` phải trùng khớp với kiểu dữ liệu của khóa chính.

**Đáp án đúng phải là:**

```java
public interface ProductRepository extends JpaRepository<Product, String> {
}

```

Nếu bạn để là `Long`, khi chạy ứng dụng Spring sẽ báo lỗi ngay lập tức vì nó không tìm thấy field `@Id` nào có kiểu `Long` trong entity Product cả.

---

## 📑 Mục Lục

- [1. "Ma thuật" sinh SQL từ tên hàm](#1-ma-thuật-sinh-sql-từ-tên-hàm)
  - [Ví dụ thực tế:](#ví-dụ-thực-tế)
- [2. Các từ khóa "Thần chú" (Keywords)](#2-các-từ-khóa-thần-chú-keywords)
- [3. Best Practices (Lưu ý của Senior)](#3-best-practices-lưu-ý-của-senior)
- [🧠 Thử thách (Coding Challenge)](#thử-thách-coding-challenge)

---

Chúng ta đã xong phần cơ bản. Bây giờ hãy đến phần "thú vị" nhất, tính năng khiến các lập trình viên Java "phát cuồng" vì Spring Data JPA: **Query Methods (Phương thức truy vấn)**.

# 📖 Module 3.1 (Phần D): Query Methods (Derived Query Methods)

## 1. "Ma thuật" sinh SQL từ tên hàm

Bình thường, để tìm user theo email, bạn phải viết SQL: `SELECT * FROM users WHERE email = ?`.
Với Spring Data JPA, bạn **KHÔNG CẦN VIẾT SQL**. Bạn chỉ cần đặt tên hàm theo đúng ngữ pháp tiếng Anh, Spring sẽ tự dịch nó ra SQL.

**Cú pháp chuẩn:**
`find` + `By` + `[Tên thuộc tính]`

### Ví dụ thực tế:

Bạn chỉ cần khai báo trong Interface:

```java
public interface UserRepository extends JpaRepository<User, Long> {
    
    // 1. Tìm theo Email
    // SQL sinh ra: SELECT * FROM users WHERE email = ?
    Optional<User> findByEmail(String email);

    // 2. Tìm theo Tên
    // SQL sinh ra: SELECT * FROM users WHERE name = ?
    List<User> findByName(String name);
}

```

Spring Data JPA sẽ tự động phân tích (parse) tên hàm:

1. Thấy chữ `find`: À, đây là lệnh `SELECT`.
2. Thấy chữ `By`: Bắt đầu điều kiện `WHERE`.
3. Thấy chữ `Email`: À, so sánh cột `email` (`WHERE email = ...`).

---

## 2. Các từ khóa "Thần chú" (Keywords)

Bạn có thể kết hợp các từ khóa để tạo ra câu truy vấn phức tạp hơn. Dưới đây là bảng "bảo bối" bạn cần nhớ:

| Keyword | Ví dụ | Ý nghĩa SQL tương ứng |
| --- | --- | --- |
| **And** | `findByEmailAndActive` | `... WHERE email = ? AND active = ?` |
| **Or** | `findByNameOrEmail` | `... WHERE name = ? OR email = ?` |
| **LessThan** | `findByAgeLessThan` | `... WHERE age < ?` |
| **GreaterThan** | `findByAgeGreaterThan` | `... WHERE age > ?` |
| **Between** | `findByAgeBetween` | `... WHERE age BETWEEN ? AND ?` |
| **Like / Containing** | `findByNameContaining` | `... WHERE name LIKE %?%` (Tìm kiếm gần đúng) |
| **OrderBy** | `findAllByOrderByNameDesc` | `... ORDER BY name DESC` |

---

## 3. Best Practices (Lưu ý của Senior)

1. **Trả về Optional:** Với các hàm tìm kiếm 1 đối tượng (như `findByEmail`), hãy luôn trả về `Optional<User>` thay vì `User` để tránh lỗi `NullPointerException`.
2. **Đừng đặt tên quá dài:** Nếu logic quá phức tạp (VD: Tìm user active, tuổi > 18, ở Hà Nội, tên bắt đầu bằng 'N'...), tên hàm sẽ dài lê thê và khó đọc.
* *Xấu:* `findByActiveTrueAndAgeGreaterThanAndCityAndNameStartingWith(...)`
* *Giải pháp:* Lúc này nên chuyển sang dùng **@Query** (chúng ta sẽ học ngay sau đây).



---

## 🧠 Thử thách (Coding Challenge)

Dựa vào bảng từ khóa ở trên, bạn hãy thử viết **tên hàm** (chỉ cần chữ ký hàm) cho các yêu cầu nghiệp vụ sau của Entity `User` (có các field: `String email`, `Integer age`, `Boolean active`):

1. **Level Dễ:** Tìm danh sách User có độ tuổi **lớn hơn** (`GreaterThan`) một số nào đó.
2. **Level Khá:** Tìm User có email trùng khớp **VÀ** đang hoạt động (`active` = true).

Bạn thử viết 2 dòng code khai báo hàm đó xem sao nhé? 👇
