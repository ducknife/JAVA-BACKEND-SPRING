Tuyệt vời! Chúng ta sẽ đi vào phần trọng tâm nhất, "vũ khí" lợi hại nhất của Spring Data JPA giúp bạn quên đi nỗi ám ảnh viết code JDBC dài dòng.

Chào mừng bạn đến với bài: **JpaRepository - Trái tim của Spring Data JPA**.

---

## 📑 Mục Lục

- [1. JpaRepository là gì?](#1-jparepository-là-gì)
  - [Cú pháp chuẩn](#cú-pháp-chuẩn)
  - [Giải mã Generics `<User, Long>`](#giải-mã-generics-user-long)
- [2. Gia phả thừa kế (Repository Hierarchy)](#2-gia-phả-thừa-kế-repository-hierarchy)
- [3. Các phương thức "Được tặng kèm" (Built-in Methods)](#3-các-phương-thức-được-tặng-kèm-built-in-methods)
  - [3.1. Nhóm Lưu & Cập nhật (`save`)](#31-nhóm-lưu-cập-nhật-save)
  - [3.2. Nhóm Tìm kiếm (`find`)](#32-nhóm-tìm-kiếm-find)
  - [3.3. Nhóm Xóa (`delete`)](#33-nhóm-xóa-delete)
- [4. Cơ chế hoạt động (Under the hood)](#4-cơ-chế-hoạt-động-under-the-hood)
- [🧠 Bài tập nhanh (Check Point)](#bài-tập-nhanh-check-point)

---

# 📖 Module 3.1 (Phần C): JpaRepository Deep Dive

## 1. JpaRepository là gì?

Nếu như trong JDBC hoặc Hibernate thuần, để làm các chức năng Thêm - Sửa - Xóa (CRUD), bạn phải viết một class `UserDAO` dài cả trăm dòng code (mở kết nối, tạo statement, set tham số, execute, đóng kết nối...), thì với **JpaRepository**, bạn chỉ cần... **tạo 1 cái Interface rỗng**.

Vâng, bạn không nghe nhầm đâu. Chỉ cần tạo Interface, không cần viết code triển khai (implements), Spring sẽ tự động làm hết cho bạn.

### Cú pháp chuẩn

```java
// File: src/main/java/com/example/project/repository/UserRepository.java

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.project.entity.User;

@Repository // Đánh dấu đây là Bean Repository (Optional nếu đã extends JpaRepository)
public interface UserRepository extends JpaRepository<User, Long> {
    // Bên trong hoàn toàn rỗng!
    // Nhưng bạn đã sở hữu hàng chục hàm CRUD xịn xò.
}

```

### Giải mã Generics `<User, Long>`

Khi kế thừa `JpaRepository`, bạn bắt buộc phải truyền 2 tham số vào dấu ngoặc nhọn `<...>`:

1. **Tham số 1 (`User`):** Đây là **Entity** mà repository này quản lý. Spring cần biết nó sẽ tạo SQL cho bảng nào.
2. **Tham số 2 (`Long`):** Đây là **kiểu dữ liệu của Khóa chính (@Id)** trong Entity đó.
* Nếu trong User bạn để `@Id private Integer id;` -> Thì ở đây phải là `<User, Integer>`.
* Nếu `@Id private String id;` -> Thì là `<User, String>`.



---

## 2. Gia phả thừa kế (Repository Hierarchy)

Để hiểu sâu, bạn cần biết `JpaRepository` không đứng một mình. Nó là "con ông cháu cha", thừa hưởng sức mạnh từ các interface cha ông.

1. **`Repository` (Cao nhất):** Chỉ là interface đánh dấu (Marker Interface), không có hàm gì cả.
2. **`CrudRepository`:** Cung cấp các hàm CRUD cơ bản nhất (`save`, `findById`, `count`, `delete`...).
3. **`PagingAndSortingRepository`:** Thêm chức năng phân trang (`Page`) và sắp xếp (`Sort`).
4. **`JpaRepository` (Chúng ta dùng cái này):**
* Kế thừa tất cả các ông trên.
* Bổ sung các hàm chuyên sâu của JPA như: `flush()` (đẩy dữ liệu ngay lập tức), `deleteInBatch()` (xóa theo lô - hiệu năng cao).



> **Lời khuyên Senior:** Luôn luôn extends `JpaRepository` trong các dự án Spring Boot thông thường vì nó đầy đủ "đồ chơi" nhất.

---

## 3. Các phương thức "Được tặng kèm" (Built-in Methods)

Ngay khi bạn extends xong, bạn có thể `@Autowired UserRepository` vào Service và dùng ngay các hàm sau mà không cần viết một dòng SQL nào:

### 3.1. Nhóm Lưu & Cập nhật (`save`)

Hàm `save()` cực kỳ thông minh. Nó dùng cho cả **Thêm mới (Create)** và **Cập nhật (Update)**.

```java
User u = new User();
u.setName("A");
userRepository.save(u); // Hibernate kiểm tra ID chưa có -> Tạo lệnh INSERT

```

```java
User u = userRepository.findById(1L).get();
u.setName("B");
userRepository.save(u); // Hibernate kiểm tra ID đã có -> Tạo lệnh UPDATE

```

### 3.2. Nhóm Tìm kiếm (`find`)

* `findAll()`: Lấy tất cả (`SELECT * FROM users`).
* `findById(ID id)`: Lấy theo khóa chính. Trả về `Optional<T>`.
* `count()`: Đếm tổng số dòng (`SELECT COUNT(*) FROM users`).
* `existsById(ID id)`: Kiểm tra tồn tại (`SELECT COUNT(*) > 0 ...`).

### 3.3. Nhóm Xóa (`delete`)

* `deleteById(ID id)`: Xóa theo ID.
* `delete(Entity e)`: Xóa một object cụ thể.
* `deleteAll()`: Xóa sạch bảng (Cẩn thận!).

---

## 4. Cơ chế hoạt động (Under the hood)

Bạn có thắc mắc: *"Tại sao Interface không có thân hàm mà lại chạy được?"*

Đây là lúc Spring sử dụng **JDK Dynamic Proxy**.
Khi ứng dụng khởi động:

1. Spring quét thấy interface `UserRepository`.
2. Nó âm thầm tạo ra một class **Implementation** (thường tên là `SimpleJpaRepository`) trong bộ nhớ.
3. Class ẩn này chứa sẵn code logic JDBC/Hibernate để thực thi các lệnh `save`, `find`...
4. Khi bạn gọi `userRepository.save()`, thực chất bạn đang gọi vào class ẩn kia.

---

## 🧠 Bài tập nhanh (Check Point)

Để chắc chắn bạn đã nắm vững cách khai báo Repository, hãy thử giải quyết tình huống sau:

Bạn có một Entity tên là `Product`, khóa chính của nó là một chuỗi ký tự (String) mã sản phẩm.

```java
@Entity
public class Product {
    @Id
    private String productCode; // Khóa chính là String
    
    private Double price;
}

```

Bạn hãy viết khai báo interface `ProductRepository` cho Entity này giúp mình nhé? (Chỉ cần dòng `public interface ...` là đủ).
