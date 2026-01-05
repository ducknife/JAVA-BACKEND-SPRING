package com.ducknife.project.modules.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // cái này = getter + setter + toString + equalsAndHashCode + RequiredArgsConstructor 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private String name;
    private double price;
    private Long category_id;
}
