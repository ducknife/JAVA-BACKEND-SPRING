package com.ducknife.project.modules.product;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/api/products")
@Slf4j // ghi lại log
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductDTO>>> showProducts() {
        log.info("-".repeat(50) + "USER: Muốn hiển thị tất cả product!" + "-".repeat(50));
        return ApiResponse.ok(productService.getProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> showProductById(@PathVariable Long id) {
        return ApiResponse.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> searchProductByNameAndPrice(
        @RequestParam String name, 
        @RequestParam double minPrice,
        @RequestParam double maxPrice
    ) {
        return ApiResponse.ok(productService.getProductsByNameAndPrice(name, minPrice, maxPrice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> updateProductName(@PathVariable Long id, @RequestBody ProductDTO product) {
        productService.updateProduct(id, product);
        return ApiResponse.ok(product);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductDTO>> addProduct(@RequestBody @Valid ProductDTO product) { // thẻ valid để check trong DTO 
        productService.addProduct(product);
        return ApiResponse.created(product);
 
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
