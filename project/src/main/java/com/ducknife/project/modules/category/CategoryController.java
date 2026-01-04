package com.ducknife.project.modules.category;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/category")
public class CategoryController {
    private final CategoryService categoryService;
    public CategoryController (CategoryService categoryService) {
        this.categoryService = categoryService;
    }
    @GetMapping
    public List<Category> showCategories() {
        return categoryService.getCategories();
    }
}
