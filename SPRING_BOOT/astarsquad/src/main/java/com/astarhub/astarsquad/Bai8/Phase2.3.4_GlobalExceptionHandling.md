

# BÀI 5: GLOBAL EXCEPTION HANDLING - KIẾN TRÚC XỬ LÝ LỖI TẬP TRUNG

## 1. Tư duy Senior: Tại sao `try-catch` là hạ sách?

Trong một hệ thống lớn, việc dùng `try-catch` trong từng Controller dẫn đến 3 thảm họa:

1. **Code rác:** Logic nghiệp vụ bị lẫn lộn với logic xử lý lỗi.
2. **Không đồng nhất:** Ông Dev A trả về lỗi kiểu `{ "code": 404 }`, ông Dev B trả về `{ "err": "Not found" }`. Frontend khóc thét.
3. **Lọt lỗi (Leak):** Nếu quên `try-catch`, Stack Trace (dòng lỗi dài ngoằng của Java) sẽ phơi bày ra cho Hacker thấy cấu trúc code của bạn.

**Giải pháp:** Sử dụng **AOP (Aspect Oriented Programming)** với `@RestControllerAdvice`.

* **Controller:** Chỉ lo đường đi nước bước (Happy Path).
* **Service:** Chỉ lo nghiệp vụ, lỗi thì cứ `throw`.
* **Global Handler:** Đứng hứng tất cả lỗi, xử lý, log, và đóng gói chuẩn `ApiResponse`.

---

## 2. Thiết kế Hệ thống Exception (Custom Hierarchy)

Chúng ta không ném `RuntimeException` chung chung. Chúng ta sẽ tạo ra một cây phả hệ lỗi để code dễ đọc hơn.

### Bước 2.1: Tạo Class cha (AppException)

Đây là gốc rễ của mọi lỗi nghiệp vụ (Business Exception).

*File: `common/exception/AppException.java*`

```java
package com.ducknife.project.common.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException {
    private final int errorCode;

    public AppException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

```

### Bước 2.2: Tạo các lỗi con cụ thể (Semantic Exceptions)

Thay vì `throw new AppException(404, ...)` ở khắp nơi, chúng ta tạo class riêng để đọc code là hiểu ngay ngữ nghĩa.

*File: `common/exception/ResourceNotFoundException.java*`

```java
package com.ducknife.project.common.exception;

public class ResourceNotFoundException extends AppException {
    public ResourceNotFoundException(String message) {
        super(404, message); // Mặc định luôn là 404
    }
}

```

*File: `common/exception/InvalidRequestException.java*` (Dùng cho lỗi 400)

```java
package com.ducknife.project.common.exception;

public class InvalidRequestException extends AppException {
    public InvalidRequestException(String message) {
        super(400, message);
    }
}

```

---

## 3. Xây dựng "Lưới Trời" (GlobalExceptionHandler)

Đây là phần quan trọng nhất. Chúng ta phải bắt được 3 loại lỗi chính:

1. **Lỗi nghiệp vụ** (Do ta tự ném: Không tìm thấy, hết hàng...).
2. **Lỗi Validation** (Do Spring ném: Dữ liệu thiếu, sai định dạng...).
3. **Lỗi hệ thống** (Bug, DB sập, NullPointer...).

*File: `common/GlobalExceptionHandler.java*`

```java
package com.ducknife.project.common;

import com.ducknife.project.common.exception.AppException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1. BẮT LỖI NGHIỆP VỤ (AppException và các con của nó)
    @ExceptionHandler(AppException.class)
    public ResponseEntity<ApiResponse<Object>> handleAppException(AppException e) {
        // Trả về đúng mã lỗi (404, 400...) và message
        return ApiResponse.error(e.getErrorCode(), e.getMessage());
    }

    // 2. BẮT LỖI VALIDATION (Khi dùng @Valid trong DTO - Bài sau sẽ học kĩ)
    // Ví dụ: Client gửi thiếu trường name, hoặc giá âm -> Spring ném lỗi này
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidation(MethodArgumentNotValidException e) {
        // Lấy lỗi đầu tiên ra để thông báo cho gọn
        String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
        
        // Validation sai thì luôn là 400 Bad Request
        return ApiResponse.error(400, errorMessage);
    }

    // 3. BẮT LỖI HỆ THỐNG (Exception.class - Lưới cuối cùng)
    // Những lỗi Dev không lường trước được (NullPointer, Database connect fail...)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Object>> handleUnwantedException(Exception e) {
        // QUAN TRỌNG: Phải Log lỗi ra để Backend biết mà sửa
        // (Sau này dùng Log4j/Sl4j, tạm thời dùng printStackTrace)
        e.printStackTrace(); 
        
        // Với Client: Tuyệt đối không trả về stack trace. Chỉ báo lỗi chung.
        return ApiResponse.error(500, "Lỗi hệ thống, vui lòng thử lại sau!");
    }
}

```

---

## 4. Cách sử dụng chuẩn Senior (Refactor Code)

Bây giờ code của bạn sẽ cực kỳ sạch ("Clean Code").

### Trong Service (Nơi chứa logic)

Sử dụng các Exception con đã tạo để code dễ đọc như văn xuôi.

```java
@Service
public class ProductService {
    
    public Product findById(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
    }

    public Product create(ProductDTO dto) {
        if (repository.existsByName(dto.getName())) {
            // Dùng lỗi 400
            throw new InvalidRequestException("Tên sản phẩm đã tồn tại!");
        }
        // ... logic tạo
    }
}

```

### Trong Controller

Không còn bóng dáng của `try-catch`.

```java
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<Product>> getById(@PathVariable Long id) {
    // Chỉ 1 dòng duy nhất
    return ApiResponse.ok(productService.findById(id));
}

```

---

## 5. Cấu hình nâng cao (Config Properties)

Mặc định, nếu User gõ sai URL (ví dụ: `/api/v1/produc` thiếu chữ t), Spring Boot sẽ tự xử lý và trả về trang trắng (Whitelabel Error Page) hoặc JSON mặc định của nó, chứ **không đi qua** `GlobalExceptionHandler` của chúng ta.

Để bắt luôn cả lỗi sai URL này (404 Handler Not Found) về một mối, bạn cần cấu hình thêm trong `application.properties`:

```properties
# Báo cho Spring biết: Nếu không tìm thấy Controller nào khớp (404), hãy ném Exception
spring.mvc.throw-exception-if-no-handler-found=true

# Tắt các file static (tránh conflict resource)
spring.web.resources.add-mappings=false

```

Sau khi thêm dòng trên, bạn bổ sung thêm 1 hàm vào `GlobalExceptionHandler` để bắt lỗi sai URL:

```java
// Import thêm NoHandlerFoundException
import org.springframework.web.servlet.NoHandlerFoundException;

@ExceptionHandler(NoHandlerFoundException.class)
public ResponseEntity<ApiResponse<Object>> handleNotFoundParams(NoHandlerFoundException e) {
    return ApiResponse.error(404, "Đường dẫn không tồn tại: " + e.getRequestURL());
}

```

---

## 6. Tổng kết bài học

Sau bài này, Project của bạn đã đạt chuẩn Enterprise về xử lý lỗi:

1. **Format thống nhất:** Dù lỗi gì (User sai, Server sập, URL sai), Client luôn nhận được JSON chuẩn `{ status, message, data: null }`.
2. **Bảo mật:** Giấu nhẹm Stack Trace, không để lộ thông tin nhạy cảm.
3. **Code sạch:** Controller và Service chỉ tập trung vào nghiệp vụ, không bị rác bởi mã xử lý lỗi.

**Bài tập bắt buộc:**

1. Implement cây `AppException`, `ResourceNotFoundException`.
2. Implement `GlobalExceptionHandler` đủ 3 phần (Business, Validation, System).
3. Refactor lại `ProductService` để ném lỗi thay vì trả null.
4. Thử gửi request sai URL xem nó có trả về JSON của mình không.

Làm xong phần này, hệ thống của bạn sẽ vững như bàn thạch. Sau đó chúng ta sẽ qua bài **Validation (DTO)** để xử lý nốt cái `@Valid` nhé!