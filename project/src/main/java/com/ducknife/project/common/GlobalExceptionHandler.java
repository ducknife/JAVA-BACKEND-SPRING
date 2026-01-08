    package com.ducknife.project.common;

    import org.springframework.dao.DuplicateKeyException;
    import org.springframework.http.ResponseEntity;
    import org.springframework.web.bind.MethodArgumentNotValidException;
    import org.springframework.web.bind.annotation.ExceptionHandler;
    import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.ducknife.project.common.exception.AppException;

    @RestControllerAdvice
    public class GlobalExceptionHandler {
        
        // bắt lỗi nghiệp vụ 
        @ExceptionHandler(AppException.class) // lấy nó và tất cả class con của nó 
        public ResponseEntity<ApiResponse<?>> handleAppException(AppException e) {
            return ApiResponse.error(e.getErrorCode(), e.getMessage());
        }

        // bắt lỗi validation 
        // Ví dụ: Client gửi thiếu trường name, hoặc giá âm -> Spring ném lỗi này
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<ApiResponse<?>> handleValidation(MethodArgumentNotValidException e) {
            String errorMessage = e.getBindingResult().getFieldError().getDefaultMessage();
            // getBindingResult(): láy kết quả đợt kiểm tra này 
            // getFieldError(): lấy lỗi đầu tiên tìm thấy
            // getDefaultMessage(): Lấy text trong message = "..." của annotation trong DTO 
            return ApiResponse.error(400, errorMessage);
        }

        // Bắt lỗi duplicateKey -> Spring bắt 
        @ExceptionHandler(DuplicateKeyException.class)
        public ResponseEntity<ApiResponse<?>> handleDuplicateKey(DuplicateKeyException e) {
            return ApiResponse.error(409, "Dữ liệu đã tồn tại trong hệ thống (Bắt ngay khi repo ăn bom từ DB)");
        }

        // Bắt lỗi client gõ sai url -> Spring bắt 
        @ExceptionHandler(NoHandlerFoundException.class)
        public ResponseEntity<ApiResponse<?>> handleNoResourceFoundException(NoHandlerFoundException e) {
            return ApiResponse.error(404, "Đường dẫn " + e.getRequestURL() + " không tồn tại!");
        }

        // Bắt lỗi hệ thống, lưới cuối cùng bắt những lỗi ko ngờ tới 
        @ExceptionHandler(Exception.class)
        public ResponseEntity<ApiResponse<?>> handleUnwantedException(Exception e) {
            // log ra trace lỗi 
            return ApiResponse.error(500, "Lỗi hệ thống, vui lòng thử lại sau!");
        }
    }
