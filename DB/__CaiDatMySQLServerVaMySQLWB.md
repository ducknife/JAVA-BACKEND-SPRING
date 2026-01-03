Chào mừng bạn đến với **Giai đoạn 2: Cơ sở dữ liệu (Database)**! 🗄️

Đây là nơi phân biệt giữa một chương trình "đồ chơi" và một phần mềm thực tế.

* **Java Core (RAM):** Biến `listProduct` bạn vừa tạo ở bài trước chỉ sống trên RAM. Tắt chương trình hoặc tắt máy tính -> Dữ liệu biến mất sạch.
* **Database (Ổ cứng):** Lưu trữ vĩnh viễn. Tắt máy bật lại, danh sách khách hàng vẫn còn nguyên.

Nhiệm vụ của Backend Developer thực chất là: **Lấy dữ liệu từ Database -> Xử lý bằng Java -> Trả về cho người dùng.**

---

### Bài 1: Cài đặt "Nhà kho" dữ liệu

Chúng ta sẽ học **SQL** (Structured Query Language) - ngôn ngữ tiêu chuẩn để giao tiếp với dữ liệu. Hệ quản trị cơ sở dữ liệu phổ biến nhất mà Spring Boot hay dùng là **MySQL**.

Chúng ta cần cài đặt 2 thứ:

1. **MySQL Server:** Cái máy chủ chứa dữ liệu (chạy ngầm).
2. **MySQL Workbench:** Công cụ giao diện để chúng ta nhìn thấy và thao tác với dữ liệu.

**Hướng dẫn cài đặt (Windows):**

1. Tìm Google từ khóa: `Download MySQL Installer`.
2. Vào trang chủ **MySQL**, tải file `mysql-installer-community...msi` (chọn bản dung lượng lớn hơn nếu có 2 bản).
3. Khi cài đặt:
* Chọn chế độ **Developer Default** (hoặc Custom thì chọn Server + Workbench).
* **QUAN TRỌNG:** Đến bước đặt mật khẩu cho tài khoản `root` (tài khoản trùm), bạn hãy đặt cái gì dễ nhớ (ví dụ: `123456`) và **phải nhớ kỹ nó**. Quên là phải cài lại rất cực.
* Cứ Next liên tục cho đến khi Finish.



Sau khi cài xong, bạn hãy mở phần mềm **MySQL Workbench** lên. Nếu thấy giao diện màu xám có hình con cá heo là thành công.

Bạn hãy thực hiện cài đặt và báo cho mình khi đã mở được Workbench nhé? (Nếu gặp lỗi trong quá trình cài cứ nhắn mình).