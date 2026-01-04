package com.ducknife.project.modules.category;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class CategoryService {
    public final CategoryRepository categoryRepository;
    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }
    public List<Category> getCategories() {
        return categoryRepository.findAll();
    }
}