Tuyệt vời! Đây là bản tổng hợp kiến thức chuyên sâu về **Entity Relationships** trong Spring Data JPA. Bài viết được trình bày chuẩn Markdown, bạn có thể copy nguyên văn vào file tài liệu của mình.

---

## 📑 Mục Lục

- [1. Bản chất: Sự xung đột giữa OOP và SQL](#1-bản-chất-sự-xung-đột-giữa-oop-và-sql)
- [2. Các loại quan hệ phổ biến](#2-các-loại-quan-hệ-phổ-biến)
- [3. Quy tắc vàng: Owning Side vs Inverse Side](#3-quy-tắc-vàng-owning-side-vs-inverse-side)
  - [Mô hình chuẩn cho quan hệ User (1) - Order (N)](#mô-hình-chuẩn-cho-quan-hệ-user-1-order-n)
- [4. Các cấu hình hành vi (Behavior)](#4-các-cấu-hình-hành-vi-behavior)
  - [4.1. Fetch Type (Chiến lược tải dữ liệu)](#41-fetch-type-chiến-lược-tải-dữ-liệu)
  - [4.2. Cascade Type (Hiệu ứng dây chuyền)](#42-cascade-type-hiệu-ứng-dây-chuyền)
- [5. Vấn đề hiệu năng: N+1 Problem](#5-vấn-đề-hiệu-năng-n1-problem)
  - [Tình huống](#tình-huống)
  - [Hậu quả](#hậu-quả)
  - [Giải pháp chuyên nghiệp](#giải-pháp-chuyên-nghiệp)

---

# 📖 Module 3.1 (Phần B): Entity Relationships (Mối quan hệ)

Trong lập trình Backend, việc thiết kế mối quan hệ giữa các Entity (Table) chính xác là yếu tố sống còn. Nếu thiết kế sai, hệ thống sẽ gặp các vấn đề nghiêm trọng về hiệu năng (N+1 problem) và tính toàn vẹn dữ liệu.

## 1. Bản chất: Sự xung đột giữa OOP và SQL

Trước khi code, bạn cần hiểu sự khác biệt cốt lõi giữa Tư duy hướng đối tượng (Java) và Cơ sở dữ liệu quan hệ (SQL).

| Đặc điểm | Database (SQL) | Java (OOP) |
| --- | --- | --- |
| **Cách liên kết** | Dùng **Foreign Key (Khóa ngoại)**. | Dùng **Object Reference (Tham chiếu)**. |
| **Vị trí** | Khóa ngoại luôn nằm ở bảng "Con" (Bảng nhiều). | Có thể nằm ở class Cha, class Con hoặc cả hai. |
| **Hướng** | Một chiều (Con trỏ về Cha). | Hai chiều (Bi-directional) hoặc Một chiều. |

---

## 2. Các loại quan hệ phổ biến

1. **@OneToMany / @ManyToOne (1-N):** Phổ biến nhất (VD: User - Orders, Class - Students).
2. **@OneToOne (1-1):** Ít dùng hơn (VD: User - UserProfile).
3. **@ManyToMany (N-N):** Phức tạp nhất (VD: Student - Course).
* *Lưu ý chuyên nghiệp:* Trong thực tế, các Senior Developer thường **tránh** dùng `@ManyToMany` trực tiếp. Thay vào đó, họ tách nó thành 2 quan hệ `@OneToMany` với một Entity trung gian (VD: `Enrollment`).



---

## 3. Quy tắc vàng: Owning Side vs Inverse Side

Đây là khái niệm quan trọng nhất để cấu hình đúng JPA.

* **Owning Side (Bên sở hữu):** Là bên **giữ khóa ngoại (Foreign Key)** trong Database.
* Luôn là phía `@ManyToOne`.
* Chịu trách nhiệm chính trong việc lưu/sửa mối quan hệ.


* **Inverse Side (Bên bị phụ thuộc):** Là bên kia của mối quan hệ.
* Luôn là phía `@OneToMany`.
* Chỉ mang ý nghĩa "Read-only" (xem danh sách) trong mắt JPA nếu không cấu hình đúng.



### Mô hình chuẩn cho quan hệ User (1) - Order (N)

#### A. Owning Side (Class Order)

Bắt buộc dùng `@JoinColumn` để chỉ định tên cột khóa ngoại.

```java
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // "user_id" là tên cột khóa ngoại trong bảng orders
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false) 
    private User user;
}

```

#### B. Inverse Side (Class User)

Bắt buộc dùng `mappedBy` để trỏ về **tên biến Java** bên Owning Side.

```java
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // mappedBy = "user" -> Trỏ vào biến 'private User user' trong class Order
    // Nếu thiếu mappedBy -> JPA sẽ tự tạo ra bảng trung gian (User_Order) -> SAI thiết kế
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Order> orders;
}

```

---

## 4. Các cấu hình hành vi (Behavior)

### 4.1. Fetch Type (Chiến lược tải dữ liệu)

Quyết định khi nào dữ liệu liên quan được tải lên từ Database.

| Loại | Cơ chế | Mặc định của | Khi nào dùng? |
| --- | --- | --- | --- |
| **EAGER** (Háo hức) | Tải dữ liệu liên quan **ngay lập tức** (dùng lệnh JOIN). | `@ManyToOne`, `@OneToOne` | Khi dữ liệu liên quan luôn luôn cần thiết (VD: Load Order thì cần biết của User nào). |
| **LAZY** (Lười biếng) | Chỉ tải khi nào **thực sự gọi đến** (VD: `.getOrders()`). | `@OneToMany`, `@ManyToMany` | Dùng cho các List danh sách để tối ưu hiệu năng. |

> **Best Practice:** Luôn ưu tiên **LAZY** cho các mối quan hệ Collection (List, Set) để tránh load quá nhiều dữ liệu thừa.

### 4.2. Cascade Type (Hiệu ứng dây chuyền)

Quyết định xem hành động trên Cha (User) có lan sang Con (Order) hay không.

* `CascadeType.PERSIST`: Lưu Cha -> Tự động lưu Con (nếu Con mới).
* `CascadeType.REMOVE`: Xóa Cha -> Tự động xóa tất cả Con.
* `CascadeType.ALL`: Bao gồm tất cả các quyền trên.

> **Cảnh báo:** Cẩn thận với `CascadeType.REMOVE`. Nếu bạn xóa `Category` (Danh mục), bạn có muốn xóa hết `Product` không? Nếu không, đừng dùng REMOVE/ALL ở đây.

---

## 5. Vấn đề hiệu năng: N+1 Problem

Đây là vấn đề kinh điển khi dùng ORM (Hibernate/JPA).

### Tình huống

Bạn có 10 User (N=10). Mỗi User cấu hình `FetchType.LAZY` cho danh sách Orders.
Bạn chạy vòng lặp in ra số lượng đơn hàng của từng User.

```java
List<User> users = userRepository.findAll(); // 1 câu Query lấy 10 User
for (User u : users) {
    System.out.println(u.getOrders().size()); // 10 câu Query lấy Order cho từng User
}

```

### Hậu quả

Tổng số câu query = **1 (Lấy cha) + N (Lấy con) = 11 câu.**
Nếu có 1000 User -> 1001 câu query -> Sập Database.

### Giải pháp chuyên nghiệp

Sử dụng **JPQL Fetch Join** để gộp thành 1 câu query duy nhất:

```java
@Query("SELECT u FROM User u JOIN FETCH u.orders")
List<User> findAllUsersWithOrders();

```

Lúc này, Hibernate sẽ dùng `INNER JOIN` trong SQL để lấy data 1 lần duy nhất.

---

Bạn đã có tài liệu chuẩn về Relationship. Bạn có muốn chúng ta đi vào thực hành ngay bài toán **"Giải quyết vấn đề N+1"** hoặc viết một **Unit Test** để chứng minh các lý thuyết trên (Fetch Type, Cascade) hoạt động đúng như mô tả không?
