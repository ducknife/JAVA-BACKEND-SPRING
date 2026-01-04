Câu hỏi rất "đắt"! Để thực sự hiểu sâu và thiết kế hệ thống chuẩn chỉ, em phải hiểu bản chất gốc rễ này.

Nếu coi **REST** là một tôn giáo, thì **Resource (Tài nguyên)** chính là "Thần thánh" của tôn giáo đó. Mọi thứ xoay quanh nó.

Dưới đây là bài phân tích chuyên sâu (Deep Dive).

---

# RESOURCE (TÀI NGUYÊN) - TRÁI TIM CỦA REST

### 1. Định nghĩa cốt lõi

Trong thế giới REST, **Resource (Tài nguyên)** là **BẤT CỨ THỨ GÌ** mà em muốn tương tác với nó.

* Nó không chỉ là dữ liệu trong Database.
* Nó là một khái niệm trừu tượng.

**Ví dụ:**

* Một sản phẩm cụ thể (Product).
* Một danh sách khách hàng (List of Customers).
* Một bức ảnh (Image).
* Thậm chí là một quy trình (Process), ví dụ: "Phiên đăng nhập" (Login Session).

> **Tư duy Senior:** Đừng nghĩ về "Hàm" (Function) hay "Thủ tục" (Procedure). Hãy nghĩ về "Vật thể" (Things).

---

### 2. Ba đặc điểm sống còn của Resource

Để một thứ được gọi là Resource trong REST, nó phải có 3 yếu tố sau:

#### A. Định danh (Identification) - URI

Mỗi tài nguyên phải có một địa chỉ nhà duy nhất. Địa chỉ này gọi là **URI (Uniform Resource Identifier)**.

* Giống như mỗi người dân có một số CCCD.
* Giống như mỗi trang web có một đường link.

**Ví dụ:**

* `http://api.shopee.com/products/iphone-15`  Đây là địa chỉ trỏ đến tài nguyên là chiếc iPhone 15.

#### B. Biểu diễn (Representation) - JSON/XML

Đây là khái niệm trừu tượng nhất nhưng quan trọng nhất.

* **Resource** là cái nằm trên Server (Là dòng dữ liệu trong database, là file ảnh trên ổ cứng).
* Nhưng Client (Frontend) không bao giờ chạm trực tiếp vào Resource đó.
* Cái mà Client nhận được chỉ là **bản sao chép (Representation)** của Resource đó tại thời điểm hiện tại.

**Ví dụ:**

* Resource: Object `Product` trong Java nằm trên RAM Server.
* Representation: Chuỗi `JSON` mà Server trả về cho React.
```json
{ "id": 1, "name": "iPhone 15" }

```


*Client đọc cái JSON này, chứ không đọc cái Object Java kia.*

#### C. Thao tác qua giao diện chuẩn (Standard Interface)

Em không được tự chế ra hành động cho Resource. Em phải dùng bộ động từ chuẩn của HTTP (GET, POST, PUT, DELETE) mà chúng ta đã học ở bài trước.

---

### 3. Tài nguyên đơn (Singleton) vs. Tập tài nguyên (Collection)

Khi thiết kế API, em phải phân biệt rõ 2 loại này để đặt tên URL cho đúng.

* **Collection Resource (Số nhiều):** Là một tập hợp danh sách.
* URL: `/products` (Toàn bộ kho hàng).
* Hành động `POST` vào đây nghĩa là: "Thêm 1 cái vào kho".


* **Singleton Resource (Số ít - Cụ thể):** Là một phần tử con trong tập hợp.
* URL: `/products/10` (Cụ thể sản phẩm số 10).
* Hành động `DELETE` vào đây nghĩa là: "Xóa thằng số 10 này đi".



---

### 4. Sub-Resource (Tài nguyên lồng nhau) - Level Senior

Đây là phần "ăn tiền" khi đi phỏng vấn hoặc thiết kế hệ thống lớn.
Trong thực tế, các tài nguyên luôn có quan hệ với nhau. Ví dụ: **Khách hàng (User)** có nhiều **Đơn hàng (Orders)**.

Làm sao để thiết kế URL cho việc: *"Lấy tất cả đơn hàng của thằng User số 5"*?

**Cách sai (Tư duy Function):**
❌ `/getOrdersByUserId?id=5` (Đây là kiểu RPC cũ, không phải REST).

**Cách đúng (Tư duy Resource):**
✅ `/users/5/orders`

**Phân tích URL này:**

1. `/users`: Vào danh sách user.
2. `/5`: Chọn thằng user số 5.
3. `/orders`: Vào danh sách đơn hàng **CỦA** thằng số 5 đó.

 Đây gọi là **Sub-resource**. Nó thể hiện mối quan hệ cha-con cực kỳ rõ ràng ngay trên đường dẫn.

---

### 5. Những sai lầm kinh điển (Anti-patterns)

Khi mới làm, em sẽ rất dễ mắc lỗi đặt tên Resource là "Động từ". Hãy tránh xa những kiểu này:

| Kiểu sai (Cấm kỵ) | Kiểu đúng (Chuẩn REST) | Lý do |
| --- | --- | --- |
| `/createNewProduct` | `POST /products` | URL phải là danh từ. Hành động do `POST` quy định. |
| `/deleteProduct/5` | `DELETE /products/5` | URL phải là danh từ. Hành động do `DELETE` quy định. |
| `/products/list` | `GET /products` | Bản thân `/products` đã là danh sách rồi, không cần thêm chữ `list`. |

---

### TỔNG KẾT

Tài nguyên (Resource) trong REST thực chất là **tư duy hướng đối tượng (OOP) áp dụng lên đường truyền mạng**.

1. Mọi thứ đều là **Danh từ** (Noun).
2. Mọi thứ đều có **Địa chỉ** (URI).
3. Mối quan hệ được thể hiện bằng **Đường dẫn lồng nhau** (Nesting).

**Thử thách cho em (Check tư duy):**
Hãy thiết kế URL (API Endpoint) cho 2 yêu cầu sau của một hệ thống trường học (School):

1. Lấy danh sách tất cả sinh viên của lớp học có ID là `IT01`.
2. Xóa một sinh viên có ID là `SV007` ra khỏi lớp `IT01` đó.

(Gợi ý: Dùng Sub-resource).