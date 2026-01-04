package com.ducknife.project.modules.category;

import java.util.List;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {
    public final CategoryRepository categoryRepository;

    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findByid(id);
    }

    public void addCategory(String nameCategory) {
        categoryRepository.save(nameCategory);
    }

    public void updateCategory(Long id, String newNameCategory) {
        categoryRepository.updateCategoryById(id, newNameCategory);
    }

    public void deleteCategory(Long id) {
        categoryRepository.delete(id);
    }

}