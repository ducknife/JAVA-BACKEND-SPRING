package com.astarhub.astarsquad;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication // = @Configuration + @EnableAutoConfiguration + @ComponentScan
// đánh dấu class là Configuration, tự động cấu hình theo dependencies rồi quét các bean 
// phải đặt ở package gốc để quét hết các package con 
public class AstarsquadApplication { 

	public static void main(String[] args) {
		SpringApplication.run(AstarsquadApplication.class, args);
	}

}

