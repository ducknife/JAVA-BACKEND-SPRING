package com.ducknife.project.modules.category;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController // là một annotation kết hợp giữa @Controller và @ResponseBody
// mọi phương thức được serialize đối tượng và trả về json/xml
@RequestMapping("/api/categories") // pattern matching, ánh xạ các web request đến các các phương thức xử lý cụ thể
                                   // trong controller
// ví dụ: request GET: /api/categories/5 thì sẽ ánh xạ đến phương thức GET:/api/categories/{id} trong controller
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public List<Category> showCategories() {
        return categoryService.getCategories();
    }

    @GetMapping("/{id}")
    public Category showCategoryById(@PathVariable Long id) {
        return categoryService.getCategoryById(id);
    }

    @PostMapping
    public void addNewCategory(@RequestBody String nameCategory) {
        categoryService.addCategory(nameCategory);
        // categoryService.addCategory(Category.builder().name("OOP").build());
    }

    @PutMapping("/{id}")
    public void updateCategoryInfo(@PathVariable Long id, @RequestBody String newNameCategory) {
        categoryService.updateCategory(id, newNameCategory);
    }

    @PatchMapping("/{id}") // PATCH chỉ cập nhật 1 trường, nếu chỉ cập nhật trường tên thì code này giống code của PUT
    public void updateCategoryName(@PathVariable Long id, @RequestBody String newNameCategory) {
        categoryService.updateCategory(id, newNameCategory);
    }

    @DeleteMapping("/{id}")
    public void deleteCategoryById(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}
