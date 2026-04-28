Tuyệt vời! Chúng ta xắn tay áo lên và bắt đầu code nào.

Đây là lúc bạn chính thức "chia tay" với cách làm cũ và bước vào thế giới của các dự án Production thực thụ.

---

## 📑 Mục Lục

- [1. Thêm Dependency (Cài đặt)](#1-thêm-dependency-cài-đặt)
- [2. Cấu hình `application.yml` (Quyết định lịch sử)](#2-cấu-hình-applicationyml-quyết-định-lịch-sử)
- [3. Quy tắc đặt tên file (Sống còn)](#3-quy-tắc-đặt-tên-file-sống-còn)
- [4. Thực hành: Khởi tạo Database (V1)](#4-thực-hành-khởi-tạo-database-v1)
- [🧠 Bài tập kiểm tra (Phần B)](#bài-tập-kiểm-tra-phần-b)

---

# 📖 Module 3.3 (Phần B): Tích hợp Flyway vào Spring Boot

## 1. Thêm Dependency (Cài đặt)

Đầu tiên, bạn cần "mời" Flyway vào dự án của mình. Nếu bạn dùng Maven (`pom.xml`), hãy thêm đoạn sau:

```xml
<!-- Flyway -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-flyway</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-flyway-test</artifactId>
            <scope>test</scope>
        </dependency>
        <!-- Loại database để flyway sử dụng -->
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-mysql</artifactId>
        </dependency>

```

---

## 2. Cấu hình `application.yml` (Quyết định lịch sử)

Đây là bước bạn tước quyền kiểm soát Database của Hibernate và giao nó cho Flyway.

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/my_database
    username: root
    password: password
  
  jpa:
    # ⚠️ QUAN TRỌNG: Tắt tính năng tự động tạo bảng của Hibernate
    # Chuyển từ 'update' sang 'validate' (Chỉ kiểm tra xem Entity có khớp với DB không)
    hibernate:
      ddl-auto: validate 
    show-sql: true

  flyway:
    # Bật Flyway
    enabled: true
    # (Tùy chọn) Xóa sạch DB cũ rồi chạy lại từ đầu nếu thấy lỗi lịch sử. 
    # TUYỆT ĐỐI KHÔNG dùng clean-on-validation-error=true trên Production!
    clean-on-validation-error: false 
    # bật tính năng baseline, tránh lỗi xung đột nếu máy đã có dữ liệu.
    baseline-on-migrate: true
    # Đặt mốc (flyway sẽ mặc định là db đã có mốc này)
    baseline-version: "1" # ví dụ ở đây là đã có V1
    out-of-order: false # nếu đã chạy tới V3 mà hôm sau chèn 1 file V2.5 thì báo lỗi luôn (xây dựng tính kỉ luật cho team). Nếu để true thì nó sẽ chạy thêm 
    # file V2.5 đó.

```

---

## 3. Quy tắc đặt tên file (Sống còn)

Flyway cực kỳ khó tính trong việc đọc tên file. Nếu bạn đặt sai 1 ký tự, nó sẽ lờ file đó đi luôn.

Cấu trúc bắt buộc: **`V<Version>__<Tên_Mô_Tả>.sql`**
*(Lưu ý: Chỗ nối giữa Version và Tên là **HAI DẤU GẠCH DƯỚI** `__`, không phải một)*

**Ví dụ đúng:**

* `V1__init_database.sql`
* `V2__add_email_to_users.sql`
* `V2.1__insert_master_data.sql` (Có thể dùng số thập phân)

**Ví dụ SAI:**

* `V1_init_db.sql` (Chỉ có 1 dấu gạch dưới -> Lỗi)
* `v2__add_column.sql` (Chữ v viết thường -> Tùy cấu hình nhưng dễ lỗi)

---

## 4. Thực hành: Khởi tạo Database (V1)

Mặc định, Flyway sẽ tìm các file SQL trong thư mục: **`src/main/resources/db/migration`**.
Bạn hãy tạo thư mục này nếu chưa có.

Bây giờ, tạo file đầu tiên: `V1__create_table_users.sql`

```sql
-- File: src/main/resources/db/migration/V1__create_table_users.sql

CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Bạn có thể chèn sẵn dữ liệu mẫu (Seeding) ngay tại đây
INSERT INTO users (username, email) VALUES ('admin', 'admin@example.com');

```

**Điều gì xảy ra khi bạn chạy ứng dụng (Run Spring Boot)?**

1. Spring Boot khởi động.
2. Flyway nhảy vào, thấy có file `V1`. Nó chạy lệnh SQL trong file đó vào DB MySQL của bạn.
3. Flyway tạo một bảng ẩn tên là `flyway_schema_history` và ghi vào đó: *"Đã chạy V1 thành công"*.
4. Tiếp theo, Hibernate khởi động, thấy cấu hình `ddl-auto: validate`. Nó đem class `@Entity User` của bạn ra so sánh với bảng `users` vừa được tạo. Khớp nhau -> Chạy tiếp. Lệch nhau -> Báo lỗi văng app.

---

## 🧠 Bài tập kiểm tra (Phần B)

Giả sử dự án của bạn đã chạy được 1 tháng và file `V1__create_table_users.sql` đã nằm ngoan ngoãn trên server Production.

Hôm nay, sếp yêu cầu bạn thêm cột `age` (tuổi) kiểu số nguyên vào bảng `users`.

Hãy viết cho mình **tên file** migration tiếp theo và **nội dung câu lệnh SQL** bên trong file đó nhé? 👇

