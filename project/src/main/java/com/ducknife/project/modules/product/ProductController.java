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
import com.ducknife.project.modules.auditlog.AuditLog;
import com.ducknife.project.modules.auditlog.AuditService;
import com.ducknife.project.modules.product.dto.ProductRequest;
import com.ducknife.project.modules.product.dto.ProductResponse;

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
    public ResponseEntity<ApiResponse<List<ProductResponse>>> showProducts() {
        log.info("-".repeat(50) + "USER: Muốn hiển thị tất cả product!" + "-".repeat(50));
        return ApiResponse.ok(productService.getProducts());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> showProductById(@PathVariable Long id) {
        return ApiResponse.ok(productService.getProductById(id));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<List<ProductResponse>>> searchProductByNameAndPrice(
        @RequestParam String name, 
        @RequestParam double minPrice,
        @RequestParam double maxPrice
    ) {
        return ApiResponse.ok(productService.getProductsByNameAndPrice(name, minPrice, maxPrice));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProductName(@PathVariable Long id, @RequestBody ProductRequest product) {
        ProductResponse updatedProduct = productService.updateProduct(id, product);
        return ApiResponse.ok(updatedProduct);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<ProductResponse>> addProduct(@RequestBody @Valid ProductRequest product) { // thẻ valid để check trong DTO 
        ProductResponse updatedProduct = productService.addProduct(product);
        return ApiResponse.created(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}
