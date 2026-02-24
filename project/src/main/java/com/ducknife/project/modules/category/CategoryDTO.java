package com.ducknife.project.modules.category;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CategoryDTO {
    @NotBlank(message = "Tên danh mục không được để trống!")
    private String name;

    public static CategoryDTO from(Category category) {
        return CategoryDTO.builder()    
                        .name(category.getName())
                        .build();
    }
}
