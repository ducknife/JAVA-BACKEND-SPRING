package com.ducknife.project.modules.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // cái này = getter + setter + toString + equalsAndHashCode + RequiredArgsConstructor 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;
    private BigDecimal price;
    private Long category_id;
}
