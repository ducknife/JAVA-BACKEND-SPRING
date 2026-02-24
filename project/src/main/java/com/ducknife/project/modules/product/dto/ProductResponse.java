package com.ducknife.project.modules.product.dto;

import java.math.BigDecimal;

import com.ducknife.project.modules.category.Category;
import com.ducknife.project.modules.product.Product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
    private Long id;
    private String name;
    private BigDecimal price;
    private Category category;
    
    public static ProductResponse from(Product product) {
        return ProductResponse.builder()
                            .id(product.getId())
                            .name(product.getName())
                            .price(product.getPrice())
                            .category(product.getCategory())
                            .build();
    }
}
