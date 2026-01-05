package com.ducknife.project.modules.category;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ducknife.project.modules.product.ProductDTO;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    public final CategoryRepository categoryRepository;

    public List<CategoryDTO> getCategorieDTOs() {
        return categoryRepository.findAll().stream()
                .map(p -> CategoryDTO.builder()
                        .name(p.getName())
                        .build())
                .collect(Collectors.toList());
    }

    public CategoryDTO getCategoryDTOById(Long id) {
        Category demandCategory = categoryRepository.findById(id);
        return CategoryDTO.builder().name(demandCategory.getName()).build();
    }

    public List<ProductDTO> getProductDTOsById(Long id) {
        return categoryRepository.findProductsById(id).stream()
                .map(p -> ProductDTO.builder()
                        .name(p.getName())
                        .price(p.getPrice())
                        .category_id(p.getCategory_id())
                        .build())
                .collect(Collectors.toList());

    }

    public List<CategoryDTO> search(String name) {
        return categoryRepository.findByName(name).stream()
                .map(c -> CategoryDTO.builder()
                        .name(c.getName())
                        .build())
                .collect(Collectors.toList());
    }

    public void addCategory(CategoryDTO categoryDTO) {
        categoryRepository.save(categoryDTO);
    }

    public void updateCategory(Long id, CategoryDTO categoryDTO) {
        categoryRepository.updateCategoryById(id, categoryDTO);
    }

    public void deleteCategory(Long id) {
        categoryRepository.delete(id);
    }

}