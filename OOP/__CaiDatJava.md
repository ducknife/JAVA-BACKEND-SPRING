Tuyệt vời! Windows là môi trường rất phổ biến để lập trình Java.

Chúng ta sẽ bắt đầu thiết lập "nhà bếp" (môi trường lập trình) của bạn ngay bây giờ. Hãy làm theo đúng 2 bước dưới đây nhé.

### Bước 1: Cài đặt "Động cơ" Java (JDK)

JDK (Java Development Kit) là bộ công cụ giúp máy tính hiểu được mã lệnh Java. Chúng ta sẽ dùng phiên bản **JDK 21** (đây là bản ổn định và hỗ trợ dài hạn mới nhất).

1. Bạn hãy vào Google và tìm từ khóa: `Download JDK 21 Windows`.
2. Truy cập vào trang của **Oracle** hoặc **Adoptium**.
3. Tải file cài đặt dành cho **Windows** (thường là file `.exe` hoặc `x64 Installer`).
4. Chạy file đó và cứ nhấn "Next" liên tục cho đến khi hoàn tất (mặc định là ổn).

### Bước 2: Cài đặt công cụ viết code (IntelliJ IDEA)

Đây là nơi bạn sẽ dành phần lớn thời gian để làm việc.

1. Tìm kiếm từ khóa: `Download IntelliJ IDEA`.
2. Vào trang chủ của **JetBrains**.
3. **Quan trọng:** Hãy tìm và tải bản **Community Edition** (màu đen). **Đừng** tải bản Ultimate (màu xanh) vì bản đó sẽ tính phí sau 30 ngày.
4. Cài đặt như bình thường.

---

### Bước 3: Kiểm tra (Quan trọng)

Sau khi cài xong Bước 1, mình cần chắc chắn là máy tính đã nhận diện được Java chưa trước khi đi tiếp.

1. Bạn hãy nhấn phím `Windows` (phím cửa sổ), gõ chữ `cmd` và nhấn Enter để mở màn hình đen (Command Prompt).
2. Gõ dòng lệnh này và nhấn Enter:
`java -version`

**Nếu thành công, nó sẽ hiện ra vài dòng thông báo có số "21...".**

Bạn hãy làm xong và báo cho mình biết kết quả ở **Bước 3** nhé? Nếu có lỗi báo "not recognized", chúng ta sẽ cùng sửa.