Chính xác! 💯

Bạn đã nắm được bản chất cốt lõi:

* **Native SQL:** Làm việc với **Database Schema** (Tên Bảng `tbl_users`, Tên Cột `email_address`).
* *Nhược điểm:* Nếu DBA đổi tên cột trong DB, code của bạn "gãy" ngay.


* **JPQL:** Làm việc với **Java Model** (Tên Class `User`, Tên Field `email`).
* *Ưu điểm:* Nếu tên bảng trong DB đổi, bạn chỉ cần sửa mapping ở file Entity (`@Column(name="new_name")`), còn câu lệnh JPQL giữ nguyên. Code bền vững hơn.



---

## 📑 Mục Lục

- [1. Tại sao cần Phân trang?](#1-tại-sao-cần-phân-trang)
- [2. Interface thần thánh: `Pageable`](#2-interface-thần-thánh-pageable)
  - [Cách dùng trong Repository](#cách-dùng-trong-repository)
  - [Cách gọi từ bên ngoài (Service/Controller)](#cách-gọi-từ-bên-ngoài-servicecontroller)
- [3. Sự khác biệt giữa `List`, `Page`, và `Slice`](#3-sự-khác-biệt-giữa-list-page-và-slice)
- [4. Sorting (Sắp xếp động)](#4-sorting-sắp-xếp-động)
- [🧠 Bài tập tổng kết Phase 3.1](#bài-tập-tổng-kết-phase-31)

---

Bây giờ, chúng ta sẽ đến với mảnh ghép cuối cùng và cực kỳ quan trọng trong Phase 3.1 này: **Pagination & Sorting (Phân trang & Sắp xếp)**.

Đây là kỹ thuật bắt buộc phải có để hệ thống không bị "sập" khi dữ liệu lớn (như Facebook hay Google không bao giờ load tất cả bài viết cùng lúc).

# 📖 Module 3.1 (Phần F): Pagination & Sorting

## 1. Tại sao cần Phân trang?

Giả sử bảng `Product` có 1 triệu dòng.

* Nếu gọi `findAll()` -> RAM đầy -> **OutOfMemoryError**.
* Giải pháp: Chỉ lấy từng phần nhỏ (Ví dụ: Trang 1 lấy 10 sản phẩm, Trang 2 lấy 10 sản phẩm tiếp theo...).

## 2. Interface thần thánh: `Pageable`

Trong Spring Data JPA, mọi thứ xoay quanh interface **`Pageable`**.
Bạn chỉ cần thêm tham số `Pageable` vào bất kỳ hàm Repository nào, Spring sẽ tự động phân trang cho bạn.

### Cách dùng trong Repository

```java
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Tìm sản phẩm theo tên, NHƯNG có phân trang
    Page<Product> findByNameContaining(String name, Pageable pageable);
}

```

### Cách gọi từ bên ngoài (Service/Controller)

Để tạo ra một đối tượng `Pageable`, ta dùng class **`PageRequest`**.

```java
// Cú pháp: PageRequest.of(số_trang, kích_thước_trang, sắp_xếp)
// Lưu ý: Số trang bắt đầu từ 0

// Ví dụ: Lấy trang 1 (page 0), mỗi trang 5 phần tử, sắp xếp theo giá giảm dần
Pageable paging = PageRequest.of(0, 5, Sort.by("price").descending());

// Gọi hàm
Page<Product> pageResult = productRepository.findByNameContaining("Phone", paging);

```

---

## 3. Sự khác biệt giữa `List`, `Page`, và `Slice`

Khi dùng phân trang, kiểu dữ liệu trả về (Return Type) rất quan trọng:

1. **`Page<T>` (Khuyên dùng cho Web Admin):**
* Trả về: Danh sách dữ liệu + **Tổng số trang** + **Tổng số phần tử**.
* *Cơ chế:* Nó chạy **2 câu lệnh SQL**.
1. `SELECT * FROM product LIMIT 5 ...` (Lấy dữ liệu).
2. `SELECT COUNT(*) FROM product ...` (Đếm tổng để biết có bao nhiêu trang).


* *Dùng khi:* Cần hiển thị thanh phân trang (1, 2, 3... 100).


2. **`Slice<T>` (Khuyên dùng cho Mobile / Infinite Scroll):**
* Trả về: Danh sách dữ liệu + Cờ báo hiệu "Có trang tiếp theo hay không" (`hasNext()`).
* *Cơ chế:* Nó chỉ chạy **1 câu lệnh SQL** (Lấy limit + 1). Không đếm tổng.
* *Ưu điểm:* Nhanh hơn `Page` vì không tốn công đếm dòng (Count query rất tốn tài nguyên với bảng lớn).
* *Dùng khi:* Chức năng "Xem thêm" hoặc cuộn vô tận (như TikTok, Facebook).


3. **`List<T>`:**
* Chỉ lấy dữ liệu, không có thông tin metadata nào thêm.



---

## 4. Sorting (Sắp xếp động)

Nếu bạn chỉ muốn sắp xếp mà không cần phân trang, hãy dùng tham số `Sort`.

```java
// Repository
List<User> findByActiveTrue(Sort sort);

// Cách dùng
Sort sortObj = Sort.by("name").ascending()
                   .and(Sort.by("age").descending()); // Sắp xếp nhiều tiêu chí
                   
List<User> users = userRepository.findByActiveTrue(sortObj);

```

---

## 🧠 Bài tập tổng kết Phase 3.1

Chúc mừng bạn! Bạn đã đi hết các kiến thức cốt lõi của Spring Data JPA:

1. **Entity** (Mapping, Lifecycle).
2. **Repository** (CRUD tự động).
3. **Query Methods** (Tự sinh SQL).
4. **@Query** (JPQL/Native).
5. **Pagination** (Xử lý dữ liệu lớn).

Để "tốt nghiệp" Phase 3.1 và chuyển sang Phase 3.2 (Database Migration & Advanced), bạn hãy giải bài toán tổng hợp (Mini-Task) này nhé:

**Đề bài:**
Giả sử bạn làm API cho trang **"Danh sách nhân viên"**.
Yêu cầu:

1. Lấy danh sách nhân viên thuộc phòng ban "IT".
2. Sắp xếp theo lương (`salary`) giảm dần.
3. Lấy trang thứ **2**, mỗi trang **10** người.

Bạn hãy viết:

1. Khai báo hàm trong Repository (dùng Query Method hoặc @Query đều được).
2. Viết 1 dòng code Java để tạo đối tượng `Pageable` đáp ứng đúng yêu cầu trên (Trang 2, Size 10, Sort Salary DESC).

Mời bạn trổ tài! 👇
-> 
