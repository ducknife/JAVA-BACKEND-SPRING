package com.ducknife.project.modules.product;

import java.net.http.HttpResponse;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequestMapping("/api/products")
@Slf4j // ghi lại log
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ApiResponse<List<ProductDTO>> showProducts() {
        log.info("-".repeat(50) + "USER: Muốn hiển thị tất cả product!" + "-".repeat(50));
        return ApiResponse.<List<ProductDTO>>builder() // truyền kiểu T lên trước builder
                .status(200)
                .message("OK")
                .data(productService.getProductDTOs())
                .build();
    }

    @GetMapping("/search")
    public ApiResponse<List<ProductDTO>> searchProductByNameAndPrice(
        @RequestParam String name, 
        @RequestParam double minPrice,
        @RequestParam double maxPrice
    ) {
        return ApiResponse.<List<ProductDTO>>builder()
                        .status(200)
                        .message("OK")
                        .data(productService.getProductDTOsByNameAndPrice(name, minPrice, maxPrice))
                        .build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<ProductDTO> updateProductName(@PathVariable Long id, @RequestBody ProductDTO product) {
        ProductDTO updatedProduct = productService.updateProduct(id, product);
        return ApiResponse.<ProductDTO>builder()
                .status(200)
                .message("OK")
                .data(updatedProduct)
                .build();
    }
}
