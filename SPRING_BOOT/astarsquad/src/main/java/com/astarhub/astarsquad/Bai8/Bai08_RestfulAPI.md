
## 1. API (Application Programming Interface)

### 📘 Định nghĩa kỹ thuật
API (Giao diện lập trình ứng dụng) là một tập hợp các quy tắc và giao thức cho phép các thành phần phần mềm khác nhau giao tiếp với nhau. Nó xác định các phương thức và định dạng dữ liệu mà một chương trình có thể sử dụng để yêu cầu dịch vụ từ một chương trình khác.

### 💡 Giải thích nôm na (Analogy: Ổ cắm điện)
Hãy tưởng tượng cái **Ổ cắm điện** trên tường.
* **Bạn (Client):** Là cái tivi, cái quạt, hay sạc điện thoại. Bạn cần điện để chạy.
* **Nhà máy điện (Server):** Nơi sản xuất và cung cấp điện năng.
* **API (Ổ cắm):** Là giao diện trung gian.
    * Bạn không cần biết nhà máy điện vận hành thế nào, đốt than hay dùng sức gió (Encapsulation - Đóng gói).
    * Bạn chỉ cần biết: Nếu cắm phích cắm 2 chân (Standard Request) vào ổ này, bạn sẽ nhận được điện 220V (Response).

> **Kết luận:** API là "cổng kết nối" giúp Frontend lấy dữ liệu từ Backend mà không cần biết Backend code bằng ngôn ngữ gì hay xử lý phức tạp ra sao.

---

## 2. RESTful API

### 📘 Định nghĩa kỹ thuật
REST (Representational State Transfer) là một **kiến trúc phần mềm** (Architectural Style), không phải là một giao thức. RESTful API là các API được thiết kế tuân theo 6 ràng buộc của REST, trong đó quan trọng nhất là:
1.  **Client-Server:** Tách biệt giao diện (Frontend) và lưu trữ dữ liệu (Backend).
2.  **Stateless (Phi trạng thái):** Server không lưu giữ thông tin về trạng thái của Client giữa các request. Mỗi request phải chứa đầy đủ thông tin cần thiết.
3.  **Uniform Interface (Giao diện đồng nhất):** Sử dụng các phương thức HTTP chuẩn (GET, POST, PUT, DELETE) để thao tác trên các **Tài nguyên** (Resources).

### 💡 Giải thích nôm na (Analogy: Quy trình gọi món)
Nếu API là người phục vụ, thì **REST** là **cuốn sổ tay quy tắc phục vụ** của nhà hàng đó để đảm bảo sự chuyên nghiệp.

* **Tài nguyên (Resource):** Trong REST, mọi thứ là danh từ. Ví dụ: `Món ăn`, `Đồ uống`. (Không dùng động từ như `LấyMónĂn`).
* **Hành động chuẩn:**
    * Muốn xem menu $\rightarrow$ Dùng lệnh **GET**.
    * Muốn gọi món mới $\rightarrow$ Dùng lệnh **POST**.
    * Muốn đổi món $\rightarrow$ Dùng lệnh **PUT**.
    * Muốn hủy món $\rightarrow$ Dùng lệnh **DELETE**.

> **Tại sao cần REST?** Để Frontend (React) và Backend (Java) có một "ngôn ngữ chung". Dù thay đổi đầu bếp (Backend) hay thay đổi khách hàng (Frontend), quy tắc gọi món vẫn không đổi.



---

## 3. @RestController

### 📘 Định nghĩa kỹ thuật
Trong Spring Framework, `@RestController` là một **Stereotype Annotation** kết hợp giữa `@Controller` và `@ResponseBody`.
* Nó đánh dấu lớp đó là một Bean được quản lý bởi Spring Context.
* Mọi phương thức trong lớp này sẽ tự động tuần tự hóa (serialize) đối tượng trả về thành **JSON** (hoặc XML) thay vì tìm kiếm một View (file HTML/JSP).

### 💡 Giải thích nôm na (Analogy: Quầy "Mang về" chuyên biệt)
* **`@Controller` thường:** Giống như quầy phục vụ ăn tại chỗ. Khi bạn gọi món, họ dọn ra bàn ghế, bát đĩa đẹp đẽ (trả về giao diện HTML).
* **`@RestController`:** Giống như quầy **Drive-thru** hoặc **Take-away**.
    * Họ không quan tâm bạn ngồi đâu.
    * Họ chỉ đóng gói món ăn vào hộp (đóng gói dữ liệu thành JSON).
    * Họ đưa cái hộp đó cho bạn, bạn muốn mang về nhà (Mobile App) hay ra công viên (Web App) ăn là việc của bạn.

```java
@RestController
public class ProductController {
    // Các hàm trong này sẽ luôn trả về DỮ LIỆU (JSON)
}

---

## 4. @RequestMapping
### 📘 Định nghĩa kỹ thuật
@RequestMapping là annotation dùng để ánh xạ (map) các web request (thông qua URL và HTTP Method) tới các phương thức xử lý (Handler Methods) cụ thể trong Controller.

* Nó có thể được cấu hình với nhiều tham số:

* value hoặc path: Đường dẫn URL (ví dụ: /api/products).

* method: Loại HTTP Method (GET, POST...).

* consumes / produces: Định dạng dữ liệu đầu vào/đầu ra (ví dụ: application/json).

### 💡 Giải thích nôm na (Analogy: Biển chỉ dẫn)
Hãy tưởng tượng một tòa nhà văn phòng lớn (Ứng dụng của bạn).

@RequestMapping("/api/v1") ở cửa chính: Đây là biển chỉ dẫn tổng. "Tất cả các dịch vụ API mời đi lối này".

@RequestMapping("/products") ở cửa phòng: "Phòng này chuyên xử lý các việc liên quan đến Sản phẩm".

Ví dụ tổng hợp
Java

@RestController // 1. Đây là quầy dịch vụ trả về dữ liệu
@RequestMapping("/api/v1/products") // 2. Địa chỉ của quầy này
public class ProductController {

    // Nếu khách đến địa chỉ trên và dùng hành động GET
    // @GetMapping là viết tắt của @RequestMapping(method = RequestMethod.GET)
    @GetMapping
    public List<String> getAllProducts() {
        return List.of("Iphone 15", "Samsung S24"); // Trả về danh sách dưới dạng JSON
    }
}

**Chính xác tuyệt đối! 10 điểm.** 👏

Bạn đã nắm rất nhanh tư duy RESTful:

* **DELETE**: Là hành động (Verb).
* `/api/v1/products`: Là tài nguyên cha (Collection).
* `/5`: Là định danh cụ thể của tài nguyên đó.

Vậy là chúng ta đã xong **Bài 1** (Khởi tạo Controller & Routing cơ bản). Bây giờ mình sẽ soạn tiếp **Bài 2** theo đúng lộ trình bạn yêu cầu: **Các phương thức HTTP (CRUD)**.

Đây là file Markdown cho **Bài 2**. Bạn copy lưu lại nhé.

---

```markdown
# Bài 2: Các Phương Thức HTTP & CRUD (Create - Read - Update - Delete)

## 1. Tổng quan
Trong RESTful API, chúng ta không đặt tên hàm kiểu "động từ" trong URL (như `/getProduct`, `/createProduct` - đây là cách sai!). Thay vào đó, chúng ta dùng các **HTTP Methods** để xác định hành động.

Spring Boot cung cấp bộ 4 annotation tương ứng với 4 thao tác dữ liệu cơ bản (CRUD).

---

## 2. @GetMapping (Read - Đọc dữ liệu)

### 📘 Định nghĩa
Dùng để xử lý yêu cầu **LẤY** dữ liệu từ Server.
* Không làm thay đổi dữ liệu trên Server (An toàn).
* Có thể lấy danh sách hoặc lấy chi tiết 1 đối tượng.

### 💡 Ví dụ thực tế
Giống như bạn **Xem Menu** hoặc **Hỏi giờ mở cửa**. Bạn chỉ nhìn và lấy thông tin, không vẽ bậy lên menu hay đổi giờ của họ.

### 💻 Code ví dụ
```java
// 1. Lấy tất cả (GET /api/v1/products)
@GetMapping
public List<Product> getAll() {
    return productService.findAll();
}

// 2. Lấy chi tiết theo ID (GET /api/v1/products/5)
@GetMapping("/{id}")
public Product getDetail(@PathVariable Long id) {
    return productService.findById(id);
}

```

---

## 3. @PostMapping (Create - Tạo mới)

### 📘 Định nghĩa

Dùng để xử lý yêu cầu **TẠO MỚI** một tài nguyên.

* Dữ liệu cần tạo thường được gửi trong phần thân (Body) của request.
* Mã phản hồi chuẩn thường là `201 Created` (thay vì `200 OK`).

### 💡 Ví dụ thực tế

Giống như bạn điền phiếu **Gọi món**. Bạn gửi một tờ phiếu (Body) vào bếp để họ làm ra một món ăn mới cho bạn.

### 💻 Code ví dụ

```java
// TẠO MỚI (POST /api/v1/products)
@PostMapping
public ResponseEntity<Product> create(@RequestBody ProductDTO dto) {
    Product newProduct = productService.save(dto);
    return ResponseEntity.status(HttpStatus.CREATED).body(newProduct);
}

```

---

## 4. @PutMapping (Update - Cập nhật)

### 📘 Định nghĩa

Dùng để xử lý yêu cầu **CẬP NHẬT** toàn bộ thông tin của một tài nguyên đã tồn tại.

* Cần biết ID của đối tượng muốn sửa.
* Gửi kèm dữ liệu mới để ghi đè lên dữ liệu cũ.

### 💡 Ví dụ thực tế

Giống như bạn báo phục vụ: *"Cho tôi đổi món Bò lúc nãy thành món Gà"*. Bạn đang thay thế món cũ.

### 💻 Code ví dụ

```java
// CẬP NHẬT (PUT /api/v1/products/5)
@PutMapping("/{id}")
public Product update(@PathVariable Long id, @RequestBody ProductDTO dto) {
    return productService.update(id, dto);
}

```

---

## 5. @DeleteMapping (Delete - Xóa)

### 📘 Định nghĩa

Dùng để xử lý yêu cầu **XÓA** một tài nguyên khỏi hệ thống.

* Thường chỉ cần ID.
* Sau khi xóa xong, thường trả về mã `204 No Content` (Xóa thành công và không còn gì để hiển thị).

### 💡 Ví dụ thực tế

Giống như bạn **Hủy món**. *"Thôi tôi không ăn món súp nữa, hủy giúp tôi"*.

### 💻 Code ví dụ

```java
// XÓA (DELETE /api/v1/products/5)
@DeleteMapping("/{id}")
public ResponseEntity<Void> delete(@PathVariable Long id) {
    productService.delete(id);
    return ResponseEntity.noContent().build(); // Trả về 204
}

```

---

## 6. Bảng tổng hợp (Cheat Sheet)

| Annotation | Method | Ý nghĩa | Body (Dữ liệu gửi kèm) | Idempotent (Lặp lại có an toàn?) |
| --- | --- | --- | --- | --- |
| `@GetMapping` | GET | Lấy dữ liệu | Không | ✅ Có (Gọi 100 lần kết quả như nhau) |
| `@PostMapping` | POST | Tạo mới | Có | ❌ Không (Gọi 2 lần -> Tạo 2 cái) |
| `@PutMapping` | PUT | Sửa đổi | Có | ✅ Có (Gọi 100 lần vẫn là sửa cái đó) |
| `@DeleteMapping` | DELETE | Xóa | Không | ✅ Có (Xóa rồi thì thôi, gọi lại vẫn thế) |

```

***

**Nhiệm vụ tiếp theo:**
Bài 3 sẽ là một phần cực kỳ quan trọng và hay gây nhầm lẫn: **Cách lấy dữ liệu từ Request**.
(Phân biệt `@PathVariable`, `@RequestParam`, `@RequestBody`).

Bạn có muốn mình đi tiếp sang bài 3 luôn không?

```
Ok, mình sẽ viết trực tiếp dưới dạng bài giảng để bạn dễ đọc và copy luôn nhé.

Đây là **Bài 3: Cách lấy dữ liệu từ Client** (Phân biệt `@PathVariable`, `@RequestParam`, `@RequestBody`).

---

# Bài 3: Lấy dữ liệu từ Request - 3 Vũ khí tối thượng

Khi Frontend (React) gửi một yêu cầu đến Backend, họ sẽ gửi kèm dữ liệu (Ví dụ: ID sản phẩm cần xóa, thông tin User mới cần tạo, hoặc từ khóa tìm kiếm).

Spring Boot cung cấp 3 annotation để "bóc tách" dữ liệu này ra khỏi Request. Đây là phần dễ nhầm lẫn nhất, nên hãy chú ý kỹ nhé.

## 1. @PathVariable (Biến đường dẫn)

### Định nghĩa chuẩn

Dùng để lấy một giá trị nằm trực tiếp **trong đường dẫn URL**. Nó thường đại diện cho **định danh duy nhất** (ID) của một tài nguyên.

### Giải thích nôm na (Analogy)

Giống như số nhà.
Khi bạn nói: *"Đến nhà số 10 phố Hàng Mã"*, thì số `10` chính là PathVariable. Nó là phần không thể thiếu của địa chỉ đó.

### Khi nào dùng?

Khi bạn muốn xác định **chính xác 1 đối tượng** (Get by ID, Delete by ID, Update by ID).

### Ví dụ Code

URL: `GET /api/v1/products/10` (Lấy sản phẩm số 10)

```java
@GetMapping("/{id}") // {id} là chỗ giữ chỗ (placeholder)
public ResponseEntity<Product> getById(@PathVariable Long id) {
    // Lúc này biến id sẽ có giá trị là 10
    return ResponseEntity.ok(productService.findById(id));
}

```

---

## 2. @RequestParam (Tham số truy vấn)

### Định nghĩa chuẩn

Dùng để lấy giá trị nằm sau dấu hỏi chấm `?` trên URL (Query String). Dữ liệu này thường mang tính chất **phụ trợ** (lọc, sắp xếp, phân trang).

### Giải thích nôm na (Analogy)

Giống như yêu cầu thêm khi gọi món.
*"Cho tôi 1 bát phở (Main Resource), nhưng **không hành** và **nhiều thịt**"*.

* Phở: Là tài nguyên.
* Không hành, nhiều thịt: Là RequestParam (Tham số phụ).

### Khi nào dùng?

Khi bạn làm tính năng **Search (tìm kiếm), Filter (lọc), Sort (sắp xếp)**.

### Ví dụ Code

URL: `GET /api/v1/products/search?name=iphone&color=black` (Tìm iphone màu đen)

```java
@GetMapping("/search")
public List<Product> search(
        @RequestParam String name, // Nhận giá trị "iphone"
        @RequestParam(required = false) String color // Nhận giá trị "black" (có thể không truyền cũng được)
) {
    return productService.search(name, color);
}

```

---

## 3. @RequestBody (Thân yêu cầu)

### Định nghĩa chuẩn

Dùng để lấy toàn bộ dữ liệu JSON nằm trong phần **Body (Thân)** của Request và map (ánh xạ) nó vào một **Java Object** (DTO).

### Giải thích nôm na (Analogy)

Giống như một cái **hồ sơ đăng ký**.
Khi bạn đi làm thủ tục, bạn điền một tờ form đầy đủ thông tin (Họ tên, ngày sinh, quê quán...) rồi đưa cả tờ giấy đó cho nhân viên. Nhân viên không đọc từng dòng trên URL mà đọc nội dung trong tờ giấy đó.

### Khi nào dùng?

Dùng cho các hành động gửi **nhiều dữ liệu** hoặc dữ liệu phức tạp: **Tạo mới (POST)** hoặc **Cập nhật (PUT)**.

### Ví dụ Code

URL: `POST /api/v1/products`
Body (JSON gửi lên):

```json
{
    "name": "Samsung S24",
    "price": 20000000,
    "description": "Mới toanh"
}

```

Code Java:

```java
@PostMapping
public ResponseEntity<Product> create(@RequestBody ProductDTO productDTO) {
    // Spring Boot tự động biến cục JSON kia thành object productDTO
    // productDTO.getName() sẽ là "Samsung S24"
    return ResponseEntity.ok(productService.save(productDTO));
}

```

---

## TỔNG KẾT (Bảng so sánh nhanh)

| Annotation | Vị trí trên URL | Ví dụ URL | Mục đích chính |
| --- | --- | --- | --- |
| **@PathVariable** | Nằm trong đường dẫn | `/products/10` | Xác định ID của đối tượng |
| **@RequestParam** | Sau dấu `?` | `/products?type=new` | Lọc, tìm kiếm, sắp xếp |
| **@RequestBody** | Ẩn trong Body | (URL không đổi) | Gửi Object lớn (Tạo/Sửa) |

---

**Bài tập nhỏ để check kiến thức:**

Nếu tôi muốn làm chức năng: **"Cập nhật giá (price) cho sản phẩm có ID là 5"**.
Frontend gửi lên:

1. URL: `/api/v1/products/5`
2. JSON Body: `{"price": 15000000}`

Trong hàm Controller, tôi sẽ phải dùng những Annotation nào kết hợp với nhau?