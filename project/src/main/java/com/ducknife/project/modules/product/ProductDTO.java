package com.ducknife.project.modules.product;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // cái này = getter + setter + toString + equalsAndHashCode + RequiredArgsConstructor 
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {
    private int status;
    private String message;
    private List<Product> data;
}
