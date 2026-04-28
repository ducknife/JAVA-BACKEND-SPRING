### KINH NGHIỆM THỰC CHIẾN: TRÁNH LỖI VẶT VỚI HIBERNATE & FLYWAY
# Dựa trên chuỗi lỗi bạn vừa xử lý, đây là 4 quy tắc sống còn khi làm việc với Spring Data JPA và Flyway:

## 📑 Mục Lục

  - [KINH NGHIỆM THỰC CHIẾN: TRÁNH LỖI VẶT VỚI HIBERNATE & FLYWAY](#kinh-nghiệm-thực-chiến-tránh-lỗi-vặt-với-hibernate-flyway)
- [1. Quy Tắc Đồng Bộ Kiểu Dữ Liệu (Data Type Consistency)](#1-quy-tắc-đồng-bộ-kiểu-dữ-liệu-data-type-consistency)
- [2. Quy Tắc Vàng Của Khóa Ngoại (Foreign Key)](#2-quy-tắc-vàng-của-khóa-ngoại-foreign-key)
- [3. Tư Duy Làm Việc Với Flyway (Flyway Mindset)](#3-tư-duy-làm-việc-với-flyway-flyway-mindset)
- [4. Quy Tắc Xử Lý Tiền Tệ (Handling Money)](#4-quy-tắc-xử-lý-tiền-tệ-handling-money)
- [5. Cẩn Thận Với @Data và @ToString Của Lombok](#5-cẩn-thận-với-data-và-tostring-của-lombok)

---
## 1. Quy Tắc Đồng Bộ Kiểu Dữ Liệu (Data Type Consistency)
* Lỗi phổ biến nhất khi dùng ddl-auto=validate là sai lệch kiểu dữ liệu giữa 3 tầng: Database (SQL) <-> Entity (Java) <-> DTO (Request/Response).
* Map đúng kiểu giữa Java và SQL:
- Long (Java) ➡️ BẮT BUỘC map với BIGINT (SQL). (Đừng dùng INT cho ID kiểu Long).
- Integer (Java) ➡️ Map với INT (SQL).
- String (Java) ➡️ Map với VARCHAR(n) (SQL).
- BigDecimal (Java) ➡️ Map với DECIMAL(precision, scale) (SQL).
* Đồng bộ các Layer trong Java:
* Khi thay đổi kiểu dữ liệu ở Entity (VD: từ Double sang BigDecimal), hãy nhớ Ctrl + Shift + F (hoặc dùng tính năng Search của IDE) để tìm và đổi luôn kiểu dữ liệu đó ở các class ...Request, ...Response, và các hàm mapper (Builder). Nếu không, @Builder hoặc MapStruct sẽ báo lỗi không ép kiểu được.
## 2. Quy Tắc Vàng Của Khóa Ngoại (Foreign Key)
* Cột Khóa ngoại (Foreign Key) BẮT BUỘC phải có cùng kiểu dữ liệu và cùng kích thước với Khóa chính (Primary Key) mà nó trỏ tới.
- Ví dụ: Nếu categories.id là BIGINT, thì products.category_id cũng phải khai báo là BIGINT. Nếu bạn khai báo là INT, Database (MySQL/PostgreSQL) sẽ từ chối tạo ràng buộc khóa ngoại (lỗi Incompatible).
## 3. Tư Duy Làm Việc Với Flyway (Flyway Mindset)
* Chuyển từ Hibernate ddl-auto sang Flyway đòi hỏi thay đổi tư duy:
- Entity không còn là "Chúa tể tạo bảng": Thêm @Version, thêm trường mới (private String address), hay đổi tên cột trong Entity... thì Database sẽ không tự thay đổi. Bạn CẦN VIẾT thêm 1 file V1.x__...sql chứa lệnh ALTER TABLE tương ứng.
- Xử lý khi Flyway báo lỗi (FAILED): Nếu file migration bị lỗi cú pháp (như lỗi Khóa ngoại ở trên), Flyway sẽ đánh dấu trạng thái success = 0 trong bảng flyway_schema_history. Sửa file SQL xong chạy lại nó vẫn sẽ chửi!
* 👉 Cách giải quyết: Bạn phải vào database xóa dòng báo lỗi đó đi (DELETE FROM flyway_schema_history WHERE success = 0;), hoặc Drop sạch schema để chạy lại từ đầu (nếu đang làm ở môi trường local).
## 4. Quy Tắc Xử Lý Tiền Tệ (Handling Money)
* Tuyệt đối KHÔNG DÙNG Float hay Double cho các trường liên quan đến tiền bạc, giá cả (price, salary, total_amount...). Lỗi làm tròn dấu phẩy động (Floating point arithmetic) sẽ gây sai lệch dữ liệu tài chính.
* Luôn dùng java.math.BigDecimal trong Java và DECIMAL(10, 2) (hoặc tùy quy mô tiền tệ) trong SQL.
Khi khởi tạo giá trị cho BigDecimal, hãy dùng new BigDecimal("100.5") (kiểu String) hoặc BigDecimal.valueOf(100.5), không truyền trực tiếp kiểu double vào toán tử new.
## 5. Cẩn Thận Với @Data và @ToString Của Lombok
*** (Điều này bạn đã note rất chuẩn trong code của bạn, mình nhắc lại để nhấn mạnh) ***
* Tuyệt đối tránh dùng @Data hoặc @ToString trên các Entity có quan hệ (@OneToMany, @ManyToOne).
* Việc in ra log (khi debug) sẽ kích hoạt tính năng Lazy Loading chạy vòng lặp vô hạn (Infinite Recursion) dẫn đến sập RAM (StackOverflowError). Hãy chỉ dùng @Getter, @Setter và tự viết hàm toString() chỉ lấy các trường cơ bản.
