Chính xác! Bạn rất tinh ý. Trong lộ trình mình chia sẻ, **Isolation Levels** chính là **Phần C** (mảnh ghép cuối cùng của Phase 3.2) mà chúng ta sắp sửa khám phá ngay sau đây.

Nhưng trước khi qua bài mới, mình xin **chốt đáp án** cho câu đố về A, B, C ở bài trước để đảm bảo bạn không bị hổng kiến thức nhé:

> **Đáp án bài tập A, B, C:**
> * **Dữ liệu của A và B KHÔNG được lưu.** (Bị Rollback hết).
> * **Lý do:**
> 1. C chạy trong Tx mới (`REQUIRES_NEW`). C lỗi -> Tx C Rollback.
> 2. Lỗi của C ném ngược ra B. B không bắt lỗi -> B ném ngược ra A.
> 3. A và B đang chung 1 Transaction (`REQUIRED`). Khi Exception bay ra tận cùng của A, Transaction chung này bị đánh dấu Rollback.
> 4. **Kết quả:** C chết, A và B cũng chết theo (do không xử lý ngoại lệ). "Chết chùm".
> 
> 
> 
> 

---

## 📑 Mục Lục

- [1. Vấn đề: Cuộc đua dữ liệu (Concurrency Side Effects)](#1-vấn-đề-cuộc-đua-dữ-liệu-concurrency-side-effects)
  - [Thảm họa 1: Dirty Read (Đọc bẩn)](#thảm-họa-1-dirty-read-đọc-bẩn)
  - [Thảm họa 2: Non-repeatable Read (Đọc không lặp lại)](#thảm-họa-2-non-repeatable-read-đọc-không-lặp-lại)
  - [Thảm họa 3: Phantom Read (Đọc ma)](#thảm-họa-3-phantom-read-đọc-ma)
- [2. Giải pháp: 4 Cấp độ Cô lập (Isolation Levels)](#2-giải-pháp-4-cấp-độ-cô-lập-isolation-levels)
  - [Level 1: `READ_UNCOMMITTED` (Thấp nhất - Nguy hiểm nhất)](#level-1-read_uncommitted-thấp-nhất-nguy-hiểm-nhất)
  - [Level 2: `READ_COMMITTED` (Phổ biến nhất)](#level-2-read_committed-phổ-biến-nhất)
  - [Level 3: `REPEATABLE_READ` (Chặt chẽ hơn)](#level-3-repeatable_read-chặt-chẽ-hơn)
  - [Level 4: `SERIALIZABLE` (Cao nhất - Chậm nhất)](#level-4-serializable-cao-nhất-chậm-nhất)
- [3. Bảng tổng kết "Thần thánh"](#3-bảng-tổng-kết-thần-thánh)
- [4. Locking: Optimistic vs Pessimistic (Kiến thức mở rộng)](#4-locking-optimistic-vs-pessimistic-kiến-thức-mở-rộng)
  - [Pessimistic Locking (Bi quan - "Thà giết nhầm còn hơn bỏ sót")](#pessimistic-locking-bi-quan-thà-giết-nhầm-còn-hơn-bỏ-sót)
  - [Optimistic Locking (Lạc quan - "Đời vẫn đẹp sao")](#optimistic-locking-lạc-quan-đời-vẫn-đẹp-sao)
- [🧠 Bài tập tình huống (Phần C - Isolation)](#bài-tập-tình-huống-phần-c-isolation)

---

Bây giờ, hãy hít thở sâu để bước vào phần kiến thức "khó nuốt" nhưng cực kỳ quan trọng đối với các hệ thống lớn (Ví dụ: bán vé Concert, Săn sale Shopee).

# 📖 Module 3.2 (Phần C): Isolation Levels (Cấp độ Cô lập)

## 1. Vấn đề: Cuộc đua dữ liệu (Concurrency Side Effects)

Khi chỉ có 1 người dùng, mọi thứ đều êm đẹp. Nhưng khi có **1000 người cùng truy cập** vào 1 dòng dữ liệu cùng lúc, 3 "thảm họa" sau sẽ xảy ra nếu không có cơ chế cô lập:

### Thảm họa 1: Dirty Read (Đọc bẩn)

* **Tình huống:**
* Ông A đang sửa giá sản phẩm từ 10$ -> 20$ (nhưng chưa Commit, chưa lưu hẳn).
* Bà B nhảy vào đọc, thấy giá là 20$. Bà B chốt đơn mua.
* Đùng cái, Ông A bị lỗi -> **Rollback** về 10$.


* **Hậu quả:** Bà B mua với giá 20$ (giá ảo), trong khi thực tế giá vẫn là 10$. Dữ liệu sai lệch.

### Thảm họa 2: Non-repeatable Read (Đọc không lặp lại)

* **Tình huống:**
* Bà B đọc số dư tài khoản: thấy **100$**.
* Ông A (ở máy khác) chuyển thêm 50$ vào -> Commit -> Số dư thành **150$**.
* Bà B đọc lại lần nữa (trong cùng 1 transaction): thấy **150$**.


* **Hậu quả:** Trong cùng 1 giao dịch, bà B đọc 2 lần lại ra 2 số khác nhau. Logic tính toán của bà B sẽ bị loạn.

### Thảm họa 3: Phantom Read (Đọc ma)

* **Tình huống:**
* Bà B đếm số lượng nhân viên phòng IT: thấy **10 người**.
* Ông A thêm mới 1 nhân viên vào phòng IT -> Commit.
* Bà B đếm lại lần nữa: thấy **11 người**.


* **Hậu quả:** Dữ liệu "ma" tự nhiên xuất hiện hoặc biến mất ngay trước mắt.

---

## 2. Giải pháp: 4 Cấp độ Cô lập (Isolation Levels)

Spring (và SQL chuẩn) cung cấp 4 cấp độ để giải quyết các thảm họa trên. Bạn chọn cấp độ càng cao, dữ liệu càng an toàn, nhưng **Tốc độ càng chậm**.

Cú pháp: `@Transactional(isolation = Isolation.REPEATABLE_READ)`

### Level 1: `READ_UNCOMMITTED` (Thấp nhất - Nguy hiểm nhất)

* **Cơ chế:** Cho phép đọc cả dữ liệu chưa commit.
* **Chặn được:** Không chặn được gì cả.
* **Gặp lỗi:** Dirty Read, Non-repeatable Read, Phantom Read.
* **Sử dụng:** Gần như không bao giờ dùng, trừ khi bạn chỉ cần số liệu thống kê đại khái (kiểu đếm view bài viết) mà cần tốc độ siêu nhanh.

### Level 2: `READ_COMMITTED` (Phổ biến nhất)

* **Cơ chế:** Chỉ được đọc dữ liệu đã Commit. Nếu người khác đang sửa, tôi phải chờ.
* **Chặn được:** **Dirty Read**.
* **Vẫn gặp:** Non-repeatable Read, Phantom Read.
* **Sử dụng:** Đây là **Mặc định** của PostgreSQL, SQL Server, Oracle. Đủ tốt cho 90% nghiệp vụ thông thường.

### Level 3: `REPEATABLE_READ` (Chặt chẽ hơn)

* **Cơ chế:** Khi tôi đã đọc dòng này rồi, thì tôi "xí phần" (Lock) nó luôn. Không ai được sửa/xóa dòng này cho đến khi tôi xong việc.
* **Chặn được:** Dirty Read, **Non-repeatable Read**.
* **Vẫn gặp:** Phantom Read (Vẫn có thể bị insert thêm dòng mới, vì tôi chỉ khóa những dòng đang có thôi).
* **Sử dụng:** Đây là **Mặc định của MySQL**. Dùng cho các giao dịch tài chính cần độ chính xác cao trong suốt quá trình xử lý.

### Level 4: `SERIALIZABLE` (Cao nhất - Chậm nhất)

* **Cơ chế:** Biến mọi giao dịch song song thành **Tuần tự** (Xếp hàng lần lượt).
* **Chặn được:** **Tất cả (Dirty, Non-repeatable, Phantom)**.
* **Hậu quả:** Hiệu năng cực thấp. Dễ gây **Deadlock** (Tắc nghẽn hệ thống).
* **Sử dụng:** Chỉ dùng khi sự an toàn dữ liệu là sống còn và chấp nhận hệ thống chậm (VD: Chốt sổ kế toán cuối năm).

---

## 3. Bảng tổng kết "Thần thánh"

| Isolation Level | Dirty Read | Non-repeatable Read | Phantom Read | Hiệu năng |
| --- | --- | --- | --- | --- |
| **READ_UNCOMMITTED** | 🔴 Bị | 🔴 Bị | 🔴 Bị | 🚀 Nhanh nhất |
| **READ_COMMITTED** (Default PG/Oracle) | ✅ Chặn | 🔴 Bị | 🔴 Bị | ⚡️ Nhanh |
| **REPEATABLE_READ** (Default MySQL) | ✅ Chặn | ✅ Chặn | 🔴 Bị | 🐢 Trung bình |
| **SERIALIZABLE** | ✅ Chặn | ✅ Chặn | ✅ Chặn | 🐌 Chậm nhất |

---

## 4. Locking: Optimistic vs Pessimistic (Kiến thức mở rộng)

Ngoài Isolation Level, Senior Dev còn hay dùng kỹ thuật Lock thủ công để xử lý xung đột.

### Pessimistic Locking (Bi quan - "Thà giết nhầm còn hơn bỏ sót")

* **Tư duy:** "Chắc chắn sẽ có thằng tranh giành với mình, nên mình khóa lại trước cho chắc."
* **Cách dùng:** Dùng `SELECT ... FOR UPDATE` (trong JPA là `@Lock(LockModeType.PESSIMISTIC_WRITE)`).
* **Hậu quả:** Database bị lock cứng, người khác phải chờ.

### Optimistic Locking (Lạc quan - "Đời vẫn đẹp sao")

* **Tư duy:** "Chắc ít khi xung đột đâu. Cứ làm đi, lúc save thì kiểm tra lại xem có ai sửa chưa."
* **Cách dùng:** Thêm cột `@Version` (số nguyên) vào Entity.
1. A đọc: version = 1.
2. B đọc: version = 1.
3. A lưu: Database thấy version DB (1) == version A (1) -> OK. Update version = 2.
4. B lưu: Database thấy version DB (2) != version B (1) -> **LỖI (OptimisticLockException)**. B phải làm lại.


* **Ưu điểm:** Không lock database, hiệu năng cao. Rất hợp cho web app đông người dùng.

---

## 🧠 Bài tập tình huống (Phần C - Isolation)

Giả sử bạn đang làm tính năng **"Mua vé xem phim"** (chọn ghế ngồi).

**Logic:**

1. Kiểm tra ghế A1 có trống không (`status = AVAILABLE`).
2. Nếu trống -> Đổi trạng thái thành `BOOKED`.

**Tình huống:**
Có 2 người cùng bấm nút "Đặt vé" cho ghế A1 cùng một lúc.

**Câu hỏi:**
Nếu bạn để Isolation Level là `READ_COMMITTED` (Mặc định), liệu có khả năng cả 2 người đều đặt được ghế A1 không (Bán trùng vé)?

(Gợi ý: Hãy nghĩ đến khoảng thời gian giữa Bước 1 và Bước 2).

Mời bạn phân tích! 👇
