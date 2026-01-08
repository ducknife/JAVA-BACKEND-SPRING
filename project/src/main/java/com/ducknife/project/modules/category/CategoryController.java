package com.ducknife.project.modules.category;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;
import com.ducknife.project.modules.product.ProductDTO;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController // là một annotation kết hợp giữa @Controller và @ResponseBody
// mọi phương thức được serialize đối tượng và trả về json/xml
@RequestMapping(path = "/api/categories", produces = "application/json") // pattern matching, ánh xạ các web request đến
                                                                         // các các phương thức xử lý cụ thể
// trong controller
// ví dụ: request GET: /api/categories/5 thì sẽ ánh xạ đến phương thức
// GET:/api/categories/{id} trong controller
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;
    
    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> showCategories() {
        return ApiResponse.ok(categoryService.getCategories());
    }

    @GetMapping("/{cid}")
    // @PathVariable và @RequestParam dùng cơ chế WebDataBinder, WebDataBinder sẽ tìm các converter tương ứng để chuyển String -> kiểu tương ứng của biến
    // nếu chuyển thành công -> gán vào biến 
    // tên biến có thể khác tên trên url nhưng phải chỉ rõ là tên nào. Ví dụ @PathVariable("id") Long id 
    public ResponseEntity<ApiResponse<CategoryDTO>> showCategoryById(@PathVariable("cid") Long id) {
        return ApiResponse.ok(categoryService.getCategoryById(id));
    }

    @GetMapping("/{id}/products")
    // PathVariable lấy giá trị nằm trực tiếp trong url, thường là các giá trị định
    // danh duy nhất cho 1 tài nguyên
    public ResponseEntity<ApiResponse<List<ProductDTO>>> showProductsByCategoryId(@PathVariable Long id) {
        return ApiResponse.ok(categoryService.getProductsByCategoryId(id));
    }

    @GetMapping("/search")
    // RequestParam lấy các giá trị sau dấu hỏi trên url, thường là các giá trị phụ
    // trợ (lọc, sắp xếp, phân trang).
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> searchCategoryByName(
            @RequestParam(required = true) String name) {

        return ApiResponse.ok(categoryService.searchByName(name));
    }

    @PostMapping
    // Request body là phần dữ liệu chính (payload) mà client gửi lên cho server
    // định dạng phổ biến nhất là JSON
    // Annotation @RequestBody sẽ tự động chuyển JSON Sang object tương ứng bằng thư
    // viện JACKSON (qua ObjectMapper) để phản tuần tự
    // Dùng reflection để quét các field sau đó tạo 1 object mới bằng default
    // Constructor rồi dùng setter để gán giá trị từ json vào
    public ResponseEntity<ApiResponse<CategoryDTO>> addNewCategory(@RequestBody @Valid CategoryDTO category) {
        categoryService.addCategory(category);
        // categoryService.addCategory(Category.builder().name("OOP").build());
        return ApiResponse.created(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategoryInfo(@PathVariable Long id, @RequestBody CategoryDTO category) {
        categoryService.updateCategory(id, category);
        return ApiResponse.ok(category);
    }

    @PatchMapping("/{id}") // PATCH chỉ cập nhật 1 trường, nếu chỉ cập nhật trường tên thì code này giống
                           // code của PUT
    public ResponseEntity<ApiResponse<CategoryDTO>> updateCategoryName(@PathVariable Long id, @RequestBody CategoryDTO category) {
        categoryService.updateCategory(id, category);
        return ApiResponse.ok(category);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategoryById(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.noContent().build();
    }
}
