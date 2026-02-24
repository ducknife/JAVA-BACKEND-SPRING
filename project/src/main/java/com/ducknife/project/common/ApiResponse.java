package com.ducknife.project.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
        private int status;
        private String message;
        private T data;

        // ĐÚNG: public static <T> List<T> myMethod(T input)
        // ↑ ↑ ↑
        // (1)Khai (2)Dùng (3)Dùng
        // báo cho output cho input
        public static <T> ResponseEntity<ApiResponse<T>> ok(T data) {
                return ResponseEntity.ok(
                                ApiResponse.<T>builder()
                                                .status(HttpStatus.OK.value()) // 200
                                                .message("Success")
                                                .data(data)
                                                .build());
        }

        public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<T>builder()
                                                .status(HttpStatus.CREATED.value()) // 201
                                                .message("Created Successfully")
                                                .data(data)
                                                .build());
        }

        public static ResponseEntity<ApiResponse<?>> error(int status, String message) {
                return ResponseEntity.status(status)
                                .body(ApiResponse.builder()
                                                .status(status)
                                                .message(message)
                                                .data(null)
                                                .build());
        }

}

// ResponseEntity dùng để kiểm soát status, header, body của HTTP Response
// (STATUS, HEADERS, BODY);
// Nếu không có .body thì phải có .build() ví dụ
// ResponseEntity.noContent().build();
// .status của ResponseEntity có 2 kiểu, 1 là truyền int, 2 là truyền
// HttpStatusCode