Xin lỗi bạn vì sự thiếu sót trong bài trước. Bạn nói rất đúng, **ACID** là nền tảng sống còn của một Backend Developer, nếu chỉ nói qua loa thì sau này gặp lỗi mất tiền hay sai số liệu sẽ không biết đường nào mà lần.

Chúng ta sẽ làm lại một bài thật sâu, thật chi tiết, mổ xẻ từng chữ cái một trong **A - C - I - D**.

Để dễ hình dung, chúng ta sẽ bám sát ví dụ kinh điển nhất: **Chuyển tiền ngân hàng** (Từ tài khoản A sang tài khoản B).

---

# 🕵️ Phân tích chuyên sâu: ACID là gì?

ACID là viết tắt của 4 tính chất bắt buộc phải có để đảm bảo một giao dịch (Transaction) an toàn tuyệt đối.

## 1. A - Atomicity (Tính Nguyên Tử)

> **Khẩu quyết:** "Một là TẤT CẢ, hai là KHÔNG CÓ GÌ." (All or Nothing).

**Giải thích:**
Trong hóa học cổ điển, Nguyên tử (Atom) là thứ không thể chia cắt được nữa. Trong Database, một Transaction (gồm nhiều bước nhỏ) cũng được coi là một khối "không thể chia cắt".

**Ví dụ tình huống:**
Chuyển 1 triệu từ A sang B gồm 2 lệnh SQL:

1. Trừ 1 triệu của A (`UPDATE account SET balance = balance - 1000 WHERE id = A`).
2. Cộng 1 triệu cho B (`UPDATE account SET balance = balance + 1000 WHERE id = B`).

**Nếu không có Atomicity (Rủi ro):**

* Lệnh 1 chạy xong (A bị trừ tiền).
* Đùng một cái **Mất điện** hoặc **Code lỗi**. Lệnh 2 chưa kịp chạy.
* **Hậu quả:** A mất tiền, B không nhận được. Tiền "bốc hơi".

**Cơ chế bảo vệ (Trong Spring/DB):**
Hệ thống sẽ giám sát. Nếu bước 2 lỗi, nó tự động **ROLLBACK** (quay ngược thời gian) bước 1. Tài khoản A trở về trạng thái ban đầu như chưa hề có cuộc giao dịch.

---

## 2. C - Consistency (Tính Nhất Quán)

> **Khẩu quyết:** "Luật là Luật. Không được phép phạm quy."

**Giải thích:**
Dữ liệu trước và sau khi giao dịch phải luôn thỏa mãn các **Quy tắc ràng buộc (Constraints)** mà bạn đã định nghĩa cho Database. Dữ liệu không được phép bị "rác" hay vô lý.

**Ví dụ tình huống:**
Ngân hàng có quy định (Constraint): **"Số dư tài khoản không được âm"** (`balance >= 0`).

**Nếu không có Consistency (Rủi ro):**

* A chỉ còn 500k.
* A cố tình chuyển 1 triệu cho B.
* Database cứ thế trừ tiền -> A còn **-500k**.
* **Hậu quả:** Dữ liệu vi phạm quy tắc nghiệp vụ.

**Cơ chế bảo vệ:**
Khi transaction định trừ tiền, Database kiểm tra ràng buộc `CHECK (balance >= 0)`. Nếu thấy sau khi trừ mà bị âm, nó sẽ **HỦY** transaction ngay lập tức và báo lỗi.

---

## 3. I - Isolation (Tính Cô Lập)

> **Khẩu quyết:** "Việc ai người nấy làm. Không nhìn trộm, không chen ngang."

**Giải thích:**
Đây là phần khó nhất và quan trọng nhất khi hệ thống có hàng nghìn người dùng cùng lúc (Concurrency). Các transaction chạy song song không được làm ảnh hưởng kết quả của nhau.

**Ví dụ tình huống (Race Condition - Cuộc đua dữ liệu):**
Tài khoản A có 10 triệu.

* **Transaction 1 (T1):** Vợ rút 10 triệu tại cây ATM.
* **Transaction 2 (T2):** Chồng rút 10 triệu trên App điện thoại.
* Cả T1 và T2 xảy ra **cùng đúng 1 tích tắc**.

**Nếu không có Isolation (Rủi ro):**

1. T1 đọc số dư: Thấy 10 triệu.
2. T2 đọc số dư: Cũng thấy 10 triệu (vì T1 chưa kịp trừ).
3. T1 trừ 10 triệu -> Còn 0 -> Nhả tiền cho vợ.
4. T2 trừ 10 triệu -> Còn 0 -> Nhả tiền cho chồng.
5. **Hậu quả:** Ngân hàng mất 10 triệu! (Tổng rút 20 triệu mà tài khoản chỉ bị trừ 10 triệu).

**Cơ chế bảo vệ:**
Database sử dụng cơ chế **Locking (Khóa)**.

* Khi T1 đang thao tác trên tài khoản A, nó sẽ **KHÓA** dòng dữ liệu đó lại.
* T2 muốn vào đọc/sửa phải **ĐỢI** T1 xong (Commit hoặc Rollback) rồi mới được vào.

---

## 4. D - Durability (Tính Bền Vững)

> **Khẩu quyết:** "Bút sa gà chết. Đã lưu là phải còn mãi."

**Giải thích:**
Một khi Transaction đã báo **"Thành công" (Commit)**, thì dữ liệu đó phải được lưu trữ vĩnh viễn, bất chấp việc giây tiếp theo server bị rút phích cắm, cháy ổ cứng hay động đất.

**Ví dụ tình huống:**

* A chuyển tiền cho B.
* Màn hình báo "Giao dịch thành công".
* 1 mili-giây sau, Server sập nguồn.

**Nếu không có Durability (Rủi ro):**
Dữ liệu lúc nãy mới chỉ nằm trên **RAM** (bộ nhớ tạm). Mất điện là mất hết. Khi khởi động lại, tiền của B biến mất.

**Cơ chế bảo vệ:**
Database sử dụng kỹ thuật **Write-Ahead Logging (WAL)**. Trước khi báo thành công, nó đã kịp ghi nhật ký giao dịch vào đĩa cứng (Hard Drive) rồi. Dù sập nguồn, khi bật lại, nó sẽ đọc nhật ký đó để khôi phục dữ liệu.

---

# 💻 Code Demo: Áp dụng vào Spring Boot

Dưới đây là cách Spring Boot hiện thực hóa ACID thông qua `@Transactional`, sử dụng đúng phong cách **Constructor Injection** mà bạn yêu cầu.

```java
@Service
@RequiredArgsConstructor // Lombok tự sinh Constructor
public class BankingService {

    private final AccountRepository accountRepository;
    private final TransactionLogRepository logRepository;

    /**
     * Hàm này đảm bảo cả 4 tính chất ACID.
     * rollbackFor = Exception.class: Đảm bảo Atomicity (Lỗi là quay lui, kể cả lỗi Checked)
     */
    @Transactional(rollbackFor = Exception.class) 
    public void transferMoney(Long senderId, Long receiverId, Double amount) throws Exception {

        // 1. ISOLATION: 
        // Khi gọi findById, nếu dùng "Select for Update" (Pessimistic Lock), 
        // dòng dữ liệu này sẽ bị khóa để người khác không chen ngang.
        Account sender = accountRepository.findById(senderId)
                .orElseThrow(() -> new RuntimeException("Người gửi không tồn tại"));

        Account receiver = accountRepository.findById(receiverId)
                .orElseThrow(() -> new RuntimeException("Người nhận không tồn tại"));

        // 2. CONSISTENCY (Kiểm tra nghiệp vụ)
        if (sender.getBalance() < amount) {
            throw new Exception("Số dư không đủ để thực hiện giao dịch!"); 
            // -> Transaction hủy, không có gì thay đổi.
        }

        // 3. ATOMICITY (Thực hiện chuỗi hành động)
        // Bước A: Trừ tiền
        sender.setBalance(sender.getBalance() - amount);
        accountRepository.save(sender);

        // Giả lập lỗi bất ngờ (Mất điện/Bug)
        if (amount > 100000000) { 
            throw new RuntimeException("Số tiền quá lớn, hệ thống bị lỗi!"); 
            // -> Nhờ Atomicity, bước A ở trên sẽ bị ROLLBACK. Tiền về lại ví người gửi.
        }

        // Bước B: Cộng tiền
        receiver.setBalance(receiver.getBalance() + amount);
        accountRepository.save(receiver);

        // Bước C: Ghi log
        logRepository.save(new TransactionLog(senderId, receiverId, amount));
        
        // 4. DURABILITY
        // Khi hàm này kết thúc (return), Spring sẽ Commit. 
        // Database ghi xuống ổ cứng. Dữ liệu an toàn mãi mãi.
    }
}

```

### Tổng kết

Bạn hãy nhớ kỹ bảng này:

| Tính chất | Ý nghĩa đời thường | Kỹ thuật xử lý |
| --- | --- | --- |
| **A**tomicity | Được ăn cả, ngã về không | Rollback |
| **C**onsistency | Làm đúng luật | Constraints (Check, FK) |
| **I**solation | Xếp hàng, không chen lấn | Locking (Khóa) |
| **D**urability | Khắc vào đá | Write-Ahead Log (Ghi đĩa) |

Bài giải thích này đã đủ chi tiết và dễ hiểu hơn cho bạn chưa? Có phần nào (A, C, I hay D) bạn muốn đào sâu thêm nữa không?