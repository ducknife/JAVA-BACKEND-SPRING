Bạn có 2 câu trả lời khá sát thực tế rồi, mình chỉnh lại một chút cho "chuẩn chỉ" về mặt kỹ thuật nhé:

1. `findByAgeGreaterThan(Integer age);`
* *Lưu ý:* Mình sửa `Long` thành `Integer` vì ở đề bài trước mình giả định `age` là `Integer`. Kiểu dữ liệu tham số phải khớp với kiểu trong Entity.


2. `findByEmailAndActive(String email, Boolean active);`
* *Lưu ý:* Bạn nhớ phải truyền đủ tham số vào trong ngoặc tương ứng với các điều kiện `And` nhé.



---

Bây giờ, giải quyết 2 câu hỏi cực hay của bạn:

### 1. Làm sao tìm "Top 3 sản phẩm bán chạy nhất"?

Spring Data JPA hỗ trợ các từ khóa giới hạn kết quả: **`Top`** hoặc **`First`**.

Giả sử trong Entity `Product` của bạn có một field là `soldCount` (số lượng đã bán). Bạn sẽ viết như sau:

```java
// Cú pháp: find + Top[Số lượng] + By + OrderBy + [Field] + Desc/Asc
List<Product> findTop3ByOrderBySoldCountDesc();

```

* **Giải nghĩa:** Tìm 3 thằng đầu tiên, sắp xếp theo số lượng bán (`soldCount`) giảm dần (`Desc`).
* **SQL sinh ra:** `SELECT * FROM product ORDER BY sold_count DESC LIMIT 3;`

### 2. Làm sao để không bị "cứng" với cái tên `findBy...`?

Bạn cảm thấy cái tên `findTop3ByOrderBySoldCountDesc` nó dài và khó nhớ đúng không? Nếu logic phức tạp hơn nữa thì tên hàm sẽ thành một bài văn luôn.

Để thoát khỏi sự gò bó này, chúng ta sẽ dùng vũ khí tiếp theo: **Annotation `@Query**`.

Đây là cách bạn tự viết câu lệnh truy vấn, và **đặt tên hàm là gì cũng được** (ngắn gọn, dễ hiểu).

---

# 📖 Module 3.1 (Phần E): @Query (Tùy biến truy vấn)

`@Query` cho phép bạn viết câu lệnh SQL (hoặc JPQL) trực tiếp bên trên hàm.

Có 2 loại query bạn có thể viết:

### Cách 1: JPQL (Java Persistence Query Language) - Khuyên dùng ⭐️

JPQL thao tác trên **Entity** (Class Java), không thao tác trên Bảng (Table).

* **Ưu điểm:** Độc lập với Database (Dùng MySQL hay PostgreSQL đều chạy được). Code hướng đối tượng.

```java
public interface ProductRepository extends JpaRepository<Product, Long> {

    // Tên hàm ngắn gọn, dễ hiểu
    @Query("SELECT p FROM Product p ORDER BY p.soldCount DESC")
    List<Product> getBestSellingProducts(Pageable pageable); // Dùng Pageable để lấy Top 3
}

```

### Cách 2: Native Query (SQL Thuần)

Viết y hệt câu lệnh bạn chạy trong MySQL Workbench.

* **Ưu điểm:** Dùng được các tính năng đặc thù của Database mà JPA không hỗ trợ.
* **Nhược điểm:** Nếu đổi Database (ví dụ từ MySQL sang Oracle) có thể phải sửa lại code.

```java
public interface ProductRepository extends JpaRepository<Product, Long> {

    // nativeQuery = true -> Báo cho Spring biết đây là SQL thuần
    @Query(value = "SELECT * FROM product ORDER BY sold_count DESC LIMIT 3", nativeQuery = true)
    List<Product> getTop3Products();
}

```

---

### 🧠 Thử thách tư duy (Critical Thinking)

Để kiểm tra bạn đã hiểu sự khác biệt giữa **JPQL** và **SQL thuần** chưa, hãy nhìn vào 2 câu lệnh sau và cho biết sự khác biệt nằm ở đâu?

1. **JPQL:** `SELECT u FROM User u WHERE u.email = ?1`
2. **SQL:** `SELECT * FROM users WHERE email_address = ?1`

(Gợi ý: Chú ý tên bảng và tên cột).