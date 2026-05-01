package com.ducknife.project.common;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import com.ducknife.project.common.exception.AppException;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
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

    // Bắt lỗi duplicateKey -> Spring bắt từ DB, dùng cho JdbcTemplate
    @ExceptionHandler(DuplicateKeyException.class)
    public ResponseEntity<ApiResponse<?>> handleDuplicateKey(DuplicateKeyException e) {
        return ApiResponse.error(409, "Dữ liệu đã tồn tại trong hệ thống (Bắt ngay khi repo ăn bom từ DB)");
    }

    // Bắt lỗi từ DB -> dùng cho Spring data jpa
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponse<?>> handleDataViolation(DataIntegrityViolationException e) {
        return ApiResponse.error(409, "Dữ liệu đã tồn tại trong hệ thống (Bắt ngay khi repo ăn bom từ DB)");
    }

    // Bắt lỗi client gõ sai url -> Spring bắt
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ApiResponse<?>> handleNoResourceFoundException(NoHandlerFoundException e) {
        return ApiResponse.error(404, "Đường dẫn " + e.getRequestURL() + " không tồn tại!");
    }

    // Bắt lỗi xác thực 
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ApiResponse<?>> handleAuthenticationException(AuthenticationException e) {
        String message = "Xác thực không thành công";
        if (e instanceof BadCredentialsException) {
            message = "Tài khoản hoặc mật khẩu không chính xác";
        }
        return ApiResponse.error(401, message);
    }

    // Bắt lỗi phân quyền
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<?>> handleAccessDeniedException(AccessDeniedException e) {
        return ApiResponse.error(403, "Bạn không có quyền truy cập vào tài nguyên này");
    }

    // Bắt lỗi hệ thống, lưới cuối cùng bắt những lỗi ko ngờ tới
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleUnwantedException(Exception e) {
        // log ra trace lỗi.0
        log.atTrace();
        return ApiResponse.error(500, "Lỗi hệ thống, vui lòng thử lại sau!");
    }
}
