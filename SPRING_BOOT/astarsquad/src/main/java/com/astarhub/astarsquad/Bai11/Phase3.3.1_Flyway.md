Tuyệt vời! Chào mừng bạn đến với mảnh ghép cuối cùng của Phase 3: **Database Migration (Quản lý phiên bản cơ sở dữ liệu)**.

Để trở thành một Backend Developer thực thụ (Production-Ready), bạn bắt buộc phải biết kỹ năng này. Ở các công ty thực tế, nếu bạn dùng `ddl-auto=update` trên môi trường Production, bạn có thể sẽ... bị đuổi việc ngay ngày đầu tiên! 😅

Như đã thống nhất, chúng ta sẽ xé nhỏ **Phase 3.3** thành các phần. Hôm nay chúng ta bắt đầu với:

---

## 📑 Mục Lục

- [1. Vấn đề của "Tuổi trẻ" (`ddl-auto=update`)](#1-vấn-đề-của-tuổi-trẻ-ddl-autoupdate)
- [2. Giải pháp của "Người trưởng thành": Database Migration](#2-giải-pháp-của-người-trưởng-thành-database-migration)
- [3. Flyway hoạt động như thế nào?](#3-flyway-hoạt-động-như-thế-nào)
- [🧠 Bài tập tư duy (Phần A)](#bài-tập-tư-duy-phần-a)
  - [1. Cơ chế Checksum (Bảo vệ tính toàn vẹn)](#1-cơ-chế-checksum-bảo-vệ-tính-toàn-vẹn)
  - [2. Tư duy "Tiến bước, không lùi" (Roll-forward)](#2-tư-duy-tiến-bước-không-lùi-roll-forward)

---

# 📖 Module 3.3 (Phần A): Tại sao phải từ bỏ `ddl-auto` và Chuyển sang Flyway?

## 1. Vấn đề của "Tuổi trẻ" (`ddl-auto=update`)

Từ đầu khóa học đến giờ, để Hibernate tự tạo bảng từ Entity, bạn thường cấu hình trong `application.yml` thế này:
`spring.jpa.hibernate.ddl-auto: update`

**Nó hoạt động thế nào?**
Khi app chạy, Hibernate sẽ nhìn vào các class `@Entity` của bạn, so sánh với Database hiện tại. Nếu thiếu bảng hay thiếu cột, nó sẽ tự sinh ra lệnh `CREATE` hoặc `ALTER` để đắp vào.

**Tại sao nó cực kỳ nguy hiểm trên Production?**

1. **Mất dữ liệu không báo trước:** Giả sử bạn đổi tên cột từ `fullName` thành `name` trong Entity. Hibernate ngu ngốc sẽ không hiểu đó là lệnh "Đổi tên" (`RENAME`). Nó sẽ hiểu là: *"À, cột fullName thừa rồi, xóa nó đi. Và thêm một cột mới tên là name"*.
-> **BÙM!** Toàn bộ tên của 1 triệu user bay màu.
2. **Không kiểm soát được:** Bạn không biết chính xác câu SQL nào sẽ được chạy vào DB. Giao DB cho một cái máy tự quyết định là một rủi ro khổng lồ.
3. **Làm việc nhóm thảm họa:** Bạn thêm cột A, đồng nghiệp thêm cột B. Lên môi trường chung, DB bị loạn cào cào vì không biết ai cập nhật trước, ai cập nhật sau.

---

## 2. Giải pháp của "Người trưởng thành": Database Migration

> **Khái niệm:** Database Migration chính là **"Git dành cho Database"**.

Thay vì để Hibernate tự đoán và sửa DB, chúng ta sẽ **tự viết các file SQL** để thay đổi DB (gọi là các script migration). Hệ thống sẽ chạy các file này theo đúng thứ tự (V1, V2, V3...).

Có 2 công cụ phổ biến nhất trong thế giới Java:

* **Flyway:** Dùng SQL thuần (dễ học, phổ biến nhất, mình khuyên dùng).
* **Liquibase:** Dùng XML/YAML/JSON để mô tả DB (phức tạp hơn, nhưng hỗ trợ nhiều loại DB đa dạng hơn).

Chúng ta sẽ chọn **Flyway** vì nó cực kỳ trực quan và đúng chuẩn Senior thực chiến.

---

## 3. Flyway hoạt động như thế nào?

Cơ chế của Flyway cực kỳ thông minh và đơn giản:

1. Khi Spring Boot khởi động, Flyway sẽ nhảy vào chạy đầu tiên (trước cả Hibernate).
2. Flyway tự động tạo một bảng đặc biệt trong DB của bạn tên là `flyway_schema_history`.
3. Nó quét thư mục chứa code của bạn (thường là `src/main/resources/db/migration`).
4. Nó đọc các file SQL trong đó (Ví dụ: `V1__Create_user_table.sql`, `V2__Add_email_column.sql`).
5. Nó so sánh với bảng `flyway_schema_history` để xem file nào **chưa được chạy**.
6. Nếu chưa chạy, nó sẽ chạy câu SQL trong file đó, và ghi lại lịch sử vào bảng history (kèm theo một mã Checksum để chống gian lận/sửa trộm file).

**Kết quả:** Dù team có 10 người, hay deploy lên 10 server khác nhau, Database luôn được cập nhật theo đúng một trình tự duy nhất, không bao giờ sai lệch.

---

## 🧠 Bài tập tư duy (Phần A)

Để chốt lại tư duy trước khi chúng ta thực sự gõ code cấu hình Flyway ở Phần B, hãy trả lời câu hỏi thực tế sau:

**Tình huống:** Bạn vừa thêm Flyway vào dự án. Thư mục migration của bạn có file `V1__Init_DB.sql` chứa lệnh tạo bảng `users`. Bạn chạy app lần đầu, Flyway tạo bảng thành công.
Hôm sau, sếp yêu cầu thêm cột `phone_number` vào bảng `users`.

**Hành động của bạn sẽ là gì?**

1. Mở file `V1__Init_DB.sql` cũ ra, viết thêm dòng `phone_number varchar(20)` vào đó rồi chạy lại app.
2. Tạo một file hoàn toàn mới tên là `V2__Add_phone_number.sql` chứa lệnh `ALTER TABLE users ADD phone_number varchar(20);` rồi chạy lại app.

Theo bạn, đâu là cách làm đúng của Database Migration và tại sao? 👇

Đáp án chuẩn xác là **Cách 2: Tạo một file hoàn toàn mới `V2__Add_phone_number.sql**`.

Dưới đây là lời giải thích chuyên sâu tại sao bạn tuyệt đối không được làm theo Cách 1:

### 1. Cơ chế Checksum (Bảo vệ tính toàn vẹn)

Khi Flyway chạy file `V1__Init_DB.sql` lần đầu tiên, nó không chỉ chạy mã SQL mà còn dùng thuật toán băm (hashing) để tạo ra một chuỗi mã hóa gọi là **Checksum** (ví dụ: `8a9b2c...`), sau đó lưu mã này vào bảng `flyway_schema_history`.

Nếu bạn mở file `V1` cũ ra và gõ thêm chữ vào, nội dung file thay đổi dẫn đến Checksum của file sẽ bị đổi thành một mã mới.

Khi bạn chạy lại ứng dụng, Flyway sẽ đối chiếu: *"Khoan đã! File V1 trên ổ cứng có Checksum khác với file V1 từng chạy trong Database"*. Ngay lập tức, nó sẽ báo lỗi **Checksum Mismatch** và **chặn ứng dụng khởi động**. Đây là tính năng bảo mật tuyệt vời để ngăn ai đó lén sửa lịch sử Database.

### 2. Tư duy "Tiến bước, không lùi" (Roll-forward)

Giống như khi bạn chơi game, qua màn rồi thì lưu file save mới (Save 2, Save 3...), chứ không lưu đè lên file Save 1 để giữ lại lịch sử.

Database Migration hoạt động y hệt như các commit trong Git. Bạn không sửa commit cũ đã đẩy lên server, mà bạn tạo một commit mới để phản ánh sự thay đổi.

* `V1`: Tạo cái móng nhà.
* `V2`: Xây tầng 1 (Thêm cột phone).
* `V3`: Xây tầng 2 (Thêm bảng orders).

---

Bạn đã nắm được triết lý cốt lõi của Database Migration rồi đó! Việc giữ cho các file script là "bất biến" (immutable) sau khi đã chạy giúp cả team luôn đồng bộ và an toàn khi đưa lên Production.

Bạn đã sẵn sàng sang **Phần B: Cấu hình Flyway vào dự án Spring Boot thực tế** chưa? Chúng ta sẽ bắt tay vào viết code và tạo cấu trúc thư mục chuẩn nhé.
