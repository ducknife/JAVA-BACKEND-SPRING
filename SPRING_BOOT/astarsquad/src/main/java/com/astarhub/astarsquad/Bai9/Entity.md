Tuyệt vời. Chúng ta sẽ bắt đầu lại phần này một cách bài bản, chuẩn mực để bạn có thể lưu làm tài liệu.

Đây là **Phần 1: Deep Dive về Entity và Cơ chế hoạt động của JPA**.

---

# 📖 Module 3.1 (Phần A): Entity & Persistence Context

## 1. Bản chất của Entity

Trong Spring Data JPA (dựa trên Hibernate), **Entity** không chỉ là một class chứa dữ liệu (DTO/POJO). Nó là một **đối tượng được ánh xạ trực tiếp (mapped)** với một bảng trong cơ sở dữ liệu và có vòng đời được quản lý chặt chẽ.

### Yêu cầu bắt buộc của một Entity

Để một class Java trở thành Entity, nó phải thỏa mãn các luật sau của JPA Specification:

1. **`@Entity`:** Đánh dấu class để Hibernate nhận biết.
2. **`@Id`:** Phải có một field làm Khóa chính (Primary Key).
3. **No-args Constructor:** Phải có một constructor rỗng (`public` hoặc `protected`).
* *Tại sao?* Hibernate sử dụng Reflection (`Class.newInstance()`) để khởi tạo đối tượng trước khi đổ dữ liệu từ DB vào. Nếu thiếu, sẽ bắn lỗi `InstantiationException`.


4. **Not Final:** Class và các method không được là `final`.
* *Tại sao?* Hibernate sử dụng kỹ thuật **Proxy** (tạo class con giả mạo class của bạn) để thực hiện tính năng Lazy Loading. Nếu class là `final`, nó không thể kế thừa để tạo Proxy.



---

## 2. Giải phẫu một Entity chuẩn (Anatomy)

Dưới đây là cấu trúc của một Entity chuyên nghiệp:

```java
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity // 1. Đánh dấu Entity
@Table(name = "users") // 2. Tùy chỉnh tên bảng (Optional)
@Getter @Setter @NoArgsConstructor @AllArgsConstructor // Lombok hỗ trợ boilerplate
public class User {

    @Id // 3. Khóa chính
    @GeneratedValue(strategy = GenerationType.IDENTITY) // 4. Chiến lược sinh khóa
    private Long id;

    @Column(name = "full_name", nullable = false, length = 100) // 5. Mapping cột chi tiết
    private String fullName;

    @Column(unique = true)
    private String email;

    // Field không muốn lưu vào DB
    @Transient 
    private String tempToken; 
}

```

### Các chiến lược sinh khóa (@GeneratedValue)

Lựa chọn `strategy` ảnh hưởng lớn đến hiệu năng:

* **`IDENTITY`** (Phổ biến nhất với MySQL): Sử dụng tính năng Auto Increment của Database.
* **`SEQUENCE`** (Phổ biến với Oracle/PostgreSQL): Sử dụng Sequence object của DB, hiệu năng cao hơn khi insert số lượng lớn (Batch Insert).
* **`UUID`**: Dùng cho các hệ thống phân tán, bảo mật cao, nhưng tốn dung lượng lưu trữ hơn.

---

## 3. Persistence Context & Entity Lifecycle (Cốt lõi)

Đây là phần kiến thức phân loại trình độ Junior và Senior.

**Persistence Context** là một "vùng nhớ đệm" (First-Level Cache) nằm giữa ứng dụng Java và Database. Mọi Entity khi làm việc đều xoay quanh 4 trạng thái sau:

| Trạng thái | Mô tả | Đặc điểm kỹ thuật |
| --- | --- | --- |
| **1. Transient** (Vô định) | Object mới tạo bằng `new`. | Chưa có ID, chưa liên kết với DB, nằm ngoài vùng nhớ JPA. |
| **2. Managed** (Bền vững) | Object đang nằm trong Persistence Context. | **QUAN TRỌNG:** Mọi thay đổi trên object này tự động đồng bộ xuống DB (Dirty Checking). |
| **3. Detached** (Tách rời) | Object có ID (từng ở trong DB) nhưng đã bị đẩy ra khỏi Context. | Thay đổi data trên object này **không** ảnh hưởng DB. Thường xảy ra khi hết Transaction. |
| **4. Removed** (Đã xóa) | Object bị đánh dấu xóa. | Sẽ bị delete khỏi DB khi commit transaction. |

---

## 4. Cơ chế "Ma thuật": Dirty Checking

Hiểu cái này, bạn sẽ hiểu tại sao đôi khi **không cần gọi `save()` mà dữ liệu vẫn được lưu**.

Khi một Entity ở trạng thái **Managed** (ví dụ: vừa được `findById` lên), Hibernate sẽ chụp một bức ảnh (snapshot) trạng thái ban đầu của nó.

Khi Transaction kết thúc (commit):

1. Hibernate so sánh trạng thái hiện tại của Object với bản snapshot.
2. Nếu thấy khác nhau (ví dụ: `name` thay đổi), nó **TỰ ĐỘNG** bắn câu lệnh `UPDATE` xuống Database.
3. Bạn không cần gọi `repository.save()`.

---

### 👨‍💻 Bài tập tình huống (Case Study)

Để chốt lại phần Entity này trước khi sang Relationship, tôi có một câu hỏi thực tế:

Giả sử bạn có đoạn code sau chạy trong một Service (có `@Transactional`):

```java
@Transactional
public void logic() {
    // 1. Tạo mới object (Transient)
    User u = new User(); 
    u.setName("A");
    
    // 2. Lưu vào DB (Chuyển sang Managed)
    u = userRepository.save(u); 

    // 3. Đổi tên
    u.setName("B");

    // HẾT HÀM (Không gọi save lần nữa)
}

```

**Câu hỏi:** Kết thúc hàm này, trong Database tên của User là "A" hay "B"? Bạn hãy giải thích dựa trên cơ chế Lifecycle ở trên.