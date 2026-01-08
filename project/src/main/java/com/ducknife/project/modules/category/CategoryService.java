package com.ducknife.project.modules.category;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ducknife.project.common.exception.ResourceConflictException;
import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.product.ProductDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    public final CategoryRepository categoryRepository;

    public List<CategoryDTO> getCategories() {
        return categoryRepository.findAll().stream()
                .map(p -> CategoryDTO.builder()
                        .name(p.getName())
                        .build())
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục sản phẩm!"));
        return CategoryDTO.builder()
                .name(category.getName())
                .build();
    }

    public List<ProductDTO> getProductsByCategoryId(Long id) {
        return categoryRepository.findProductsById(id).stream()
                .map(p -> ProductDTO.builder()
                        .name(p.getName())
                        .price(p.getPrice())
                        .category_id(p.getCategory_id())
                        .build())
                .collect(Collectors.toList());

    }

    public List<CategoryDTO> searchByName(String name) {
        return categoryRepository.findByName(name).stream()
                .map(c -> CategoryDTO.builder()
                        .name(c.getName())
                        .build())
                .collect(Collectors.toList());
    }

    public void addCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new ResourceConflictException("Danh mục sản phẩm " + categoryDTO.getName() + " đã tồn tại! (Bắt khi service thấy)");
        }
        categoryRepository.save(categoryDTO);
    }

    public void updateCategory(Long id, CategoryDTO categoryDTO) {
        int affectedRows = categoryRepository.updateCategoryById(id, categoryDTO);
        if (affectedRows == 0) throw new ResourceNotFoundException("Danh mục sản phẩm không tồn tại!");
    }

    public void deleteCategory(Long id) {
        int affectedRows = categoryRepository.delete(id);
        if (affectedRows == 0) throw new ResourceNotFoundException("Không thể xóa danh mục sản phẩm không tồn tại!");
    }

}