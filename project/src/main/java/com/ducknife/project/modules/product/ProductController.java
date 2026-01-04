package com.ducknife.project.modules.product;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@Slf4j // ghi lại log 
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ProductDTO showProducts() {
        log.info("-".repeat(50) + "USER: Muốn hiển thị tất cả product!" + "-".repeat(50));
        return ProductDTO.builder()
                        .status(200)
                        .message("OK")
                        .data(productService.getProducts())
                        .build();
    }
}
