package com.ducknife.project.modules.product;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // cái này = getter + setter + toString + equalsAndHashCode + RequiredArgsConstructor 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String name;
    private double price;
    private Long category_id;
}
