package com.ducknife.project.common.exception;

import lombok.Getter;

@Getter
public class AppException extends RuntimeException{
    private final int errorCode;

    public AppException(int errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}

// kế thừa RuntimeException vì đây là kiểu unchecked (không bắt buộc khai báo) 
// còn Exception là lỗi bắt buộc khai báo, nếu dùng thì mọi hàm sử dụng phải throws AppException hoặc try - catch 
