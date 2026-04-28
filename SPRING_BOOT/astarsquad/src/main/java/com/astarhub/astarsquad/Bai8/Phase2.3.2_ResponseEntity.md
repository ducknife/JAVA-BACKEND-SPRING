
# PHẦN 4: RESPONSE ENTITY & NGHỆ THUẬT PHẢN HỒI API

### 1. Bản chất: Gói tin HTTP thực sự trông như thế nào?

Khi Server (Backend) trả lời cho Client (Frontend), nó không chỉ ném về một cục dữ liệu. Nó gửi một **Gói tin (Response Packet)** gồm 2 phần tách biệt:

1. **HEADER (Cái vỏ phong bì):** Chứa các thông tin điều khiển. Quan trọng nhất là **HTTP Status Code** (Mã trạng thái).
2. **BODY (Ruột thư):** Chứa dữ liệu thực tế (JSON).

**Vấn đề:**

* Nếu bạn chỉ `return Product;`  Spring Boot chỉ tạo được cái **BODY**. Phần **HEADER** nó tự động set mặc định là `200 OK`.
* **Hậu quả:** Khi bạn muốn báo lỗi (404) hoặc báo tạo thành công (201), bạn không có cách nào can thiệp vào cái HEADER kia cả.

 **`ResponseEntity`** sinh ra để bạn nắm quyền kiểm soát **cả HEADER lẫn BODY**.

---

## 📑 Mục Lục

  - [1. Bản chất: Gói tin HTTP thực sự trông như thế nào?](#1-bản-chất-gói-tin-http-thực-sự-trông-như-thế-nào)
  - [2. Bộ từ vựng của Web: HTTP Status Codes](#2-bộ-từ-vựng-của-web-http-status-codes)
  - [3. Mô hình chuẩn Enterprise: "Hộp trong Hộp"](#3-mô-hình-chuẩn-enterprise-hộp-trong-hộp)
  - [4. Tại sao phải làm phức tạp thế này? (Tư duy Senior)](#4-tại-sao-phải-làm-phức-tạp-thế-này-tư-duy-senior)
  - [BÀI TẬP THỰC HÀNH](#bài-tập-thực-hành)

---

### 2. Bộ từ vựng của Web: HTTP Status Codes

Để code chuẩn Senior, bạn phải thuộc "bảng cửu chương" này. Đây là ngôn ngữ để Backend và Frontend hiểu nhau mà không cần nói lời nào.

| Mã | Tên (Status) | Ý nghĩa nghiệp vụ | Khi nào dùng? |
| --- | --- | --- | --- |
| **200** | **OK** | Thành công. | Lấy dữ liệu (GET), Sửa (PUT). |
| **201** | **Created** | Đã tạo xong. | Tạo mới (POST). |
| **204** | **No Content** | Xong rồi, không có gì để trả về. | Xóa (DELETE). |
| **400** | **Bad Request** | Dữ liệu gửi lên bị sai. | Validate lỗi (VD: Giá âm, thiếu tên). |
| **401** | **Unauthorized** | Chưa đăng nhập. | Khi User chưa có Token. |
| **403** | **Forbidden** | Không có quyền. | User thường đòi xóa dữ liệu Admin. |
| **404** | **Not Found** | Không tìm thấy. | Sai ID. |
| **500** | **Internal Server Error** | Lỗi sập Server. | Code bị NullPointer, DB bị chết. |

---

### 3. Mô hình chuẩn Enterprise: "Hộp trong Hộp"

Ở level chuyên nghiệp, chúng ta không bao giờ trả về dữ liệu trần trụi. Chúng ta dùng cấu trúc **"Hộp trong Hộp"**:

* **Lớp vỏ ngoài (ResponseEntity):** Điều khiển đèn tín hiệu giao thông (HTTP Status Code) cho trình duyệt/máy móc hiểu.
* **Lớp vỏ trong (ApiResponse):** Chuẩn hóa định dạng JSON để con người/Frontend dễ đọc code.

#### Bước 1: Tạo class chuẩn hóa dữ liệu (ApiResponse)

Bạn giữ nguyên class `ApiResponse` trong package `common` như đã có. Đây là thiết kế rất tốt.

```java
package com.ducknife.project.common;

import lombok.*;

@Data
@Builder
public class ApiResponse<T> {
    private int status;       // Mã lỗi (để frontend hiện ra màn hình nếu cần)
    private String message;   // Thông báo (VD: "Thành công", "Lỗi rồi")
    private T data;           // Dữ liệu chính (Product, List<User>...)

    // Helper method 1: Dùng cho THÀNH CÔNG (200, 201)
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(data)
                .build();
    }

    // Helper method 2: Dùng cho LỖI (400, 404, 500)
    public static <T> ApiResponse<T> error(int status, String message) {
        return ApiResponse.<T>builder()
                .status(status)
                .message(message)
                .data(null)
                .build();
    }
}

```

#### Bước 2: Viết Controller chuẩn "Sách giáo khoa"

Bây giờ, hãy áp dụng vào `ProductController`. Hãy nhìn kỹ sự phối hợp nhịp nhàng giữa `ResponseEntity` và `ApiResponse`.

```java
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // 1. GET ALL (200 OK)
    @GetMapping
    public ResponseEntity<ApiResponse<List<Product>>> getAll() {
        List<Product> list = productService.findAll();
        
        // Trả về: Header 200 + Body JSON chuẩn
        return ResponseEntity.ok(ApiResponse.success(200, "Lấy danh sách thành công", list));
    }

    // 2. GET BY ID (200 OK hoặc 404 Not Found)
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Product>> getById(@PathVariable Long id) {
        // Giả sử Service trả về Product (đã handle lỗi ở Service rồi - bài sau sẽ học)
        Product product = productService.findById(id);

        return ResponseEntity.ok(ApiResponse.success(200, "Tìm thấy sản phẩm", product));
    }

    // 3. CREATE (201 Created) - CHÚ Ý CHỖ NÀY
    @PostMapping
    public ResponseEntity<ApiResponse<Product>> create(@RequestBody ProductDTO dto) {
        Product newProduct = productService.create(dto);

        // Header là 201 (Quan trọng!)
        return ResponseEntity.status(HttpStatus.CREATED) 
                .body(ApiResponse.success(201, "Tạo mới thành công", newProduct));
    }

    // 4. DELETE (204 No Content)
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        
        // Với Delete, thường không cần trả về Body (JSON), chỉ cần Header báo 204 là đủ
        return ResponseEntity.noContent().build();
    }
}

```

---

### 4. Tại sao phải làm phức tạp thế này? (Tư duy Senior)

Bạn có thể thắc mắc: *"Sao không return luôn cái List cho nhanh?"*

Đây là lý do tại sao các công ty lớn (Shopee, Grab, Facebook) đều làm theo cách trên:

1. **Frontend nhàn hạ:** Frontend Dev chỉ cần viết 1 hàm duy nhất để xử lý kết quả.
* Cứ thấy `data` là lấy dữ liệu.
* Cứ thấy `message` là hiện thông báo.
* Họ không cần đoán xem hôm nay bạn trả về cái gì.


2. **Mở rộng dễ dàng:** Sau này sếp bảo: *"Thêm cho anh cái thời gian server trả lời vào API"*. Bạn chỉ cần thêm trường `timestamp` vào class `ApiResponse`. Tất cả 100 API trong dự án tự động có thêm trường đó mà không cần sửa từng Controller.
3. **Tách biệt lỗi:** HTTP Header (200/404) giúp các hệ thống giám sát (Monitoring Tools) biết server có khỏe không. Còn JSON Body giúp con người biết chi tiết lỗi là gì.

---

### BÀI TẬP THỰC HÀNH

Bây giờ, bạn hãy thực hiện các bước sau vào dự án của mình:

1. Update file `ApiResponse.java` thêm các hàm `success` và `error` như code mẫu Bước 1.
2. Viết lại `ProductController` dùng `ResponseEntity<ApiResponse<...>>` như code mẫu Bước 2.
3. Chạy thử Postman method **POST**:
* Kiểm tra xem **Status** (góc trên bên phải Postman) có phải là `201 Created` không?
* Kiểm tra xem **Body** có ra đúng định dạng `{"status": 201, "message": "...", "data": ...}` không?



Nếu cả 2 đều đúng, chúc mừng bạn! Bạn đã viết được API chuẩn mực mà mọi Senior đều gật đầu đồng ý.
