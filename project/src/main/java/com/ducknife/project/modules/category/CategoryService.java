package com.ducknife.project.modules.category;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ducknife.project.common.exception.ResourceConflictException;
import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.product.ProductRepository;
import com.ducknife.project.modules.product.dto.ProductResponse;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    public final CategoryRepository categoryRepository;
    public final ProductRepository productRepository;

    public List<CategoryDTO> getCategories() {
        return categoryRepository.findProductsByCategory().stream()
                .map(p -> CategoryDTO.builder()
                        .name(p.getName() + ": " + p.getProducts().size())
                        .build())
                .collect(Collectors.toList());
    }

    @Transactional
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục sản phẩm!"));
        // ko cần save do đang managed nếu dùng JPA/hibernate;
        // category.setName("Test Persist of Entity"); <- Test Persist/Managed của entity;
        // còn JdcbTemplate ko có dirty checking 
        return CategoryDTO.builder()
                .name(category.getName())
                .build();
    }
    // hết hàm này object category kia chuyển về detached(), khi đó mọi thay đổi không ảnh hưởng đến DB.

    public List<ProductResponse> getProductsByCategoryId(Long id) {
        Category category = categoryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục sản phẩm!"));

        return productRepository.findByCategoryId(id).stream()
                .map(p -> ProductResponse.builder()
                        .id(p.getId())
                        .name(p.getName())
                        .price(p.getPrice())
                        .category_id(category.getId())
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
            throw new ResourceConflictException(
                    "Danh mục sản phẩm " + categoryDTO.getName() + " đã tồn tại! (Bắt khi service thấy)");
        }
        Category newCategory = Category.builder()
                                .name(categoryDTO.getName())
                                .build();
        categoryRepository.save(newCategory);
    }

    public void updateCategory(Long id, CategoryDTO categoryDTO) {
        if (!categoryRepository.existsById(id))
            throw new ResourceNotFoundException("Danh mục sản phẩm không tồn tại!");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục sản phẩm không tồn tại!"));
        category.setName(categoryDTO.getName());
        categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id))
            throw new ResourceNotFoundException("Không thể xóa danh mục sản phẩm không tồn tại!");
        categoryRepository.deleteById(id);
    }

}