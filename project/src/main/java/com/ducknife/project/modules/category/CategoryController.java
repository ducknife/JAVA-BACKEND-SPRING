package com.ducknife.project.modules.category;

import java.util.List;

import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ducknife.project.common.ApiResponse;
import com.ducknife.project.modules.product.ProductDTO;

import lombok.RequiredArgsConstructor;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

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
    public ApiResponse<List<CategoryDTO>> showCategories() {
        List<CategoryDTO> categoryDTOs = categoryService.getCategorieDTOs();
        return ApiResponse.<List<CategoryDTO>>builder()
                .status(200)
                .message("OK")
                .data(categoryDTOs)
                .build();
    }

    @GetMapping("/{cid}")
    // @PathVariable và @RequestParam dùng cơ chế WebDataBinder, WebDataBinder sẽ tìm các converter tương ứng để chuyển String -> kiểu tương ứng của biến
    // nếu chuyển thành công -> gán vào biến 
    // tên biến có thể khác tên trên url nhưng phải chỉ rõ là tên nào. Ví dụ @PathVariable("id") Long id 
    public ApiResponse<CategoryDTO> showCategoryById(@PathVariable("cid") Long id) {
        return ApiResponse.<CategoryDTO>builder()
                .status(200)
                .message("OK")
                .data(categoryService.getCategoryDTOById(id))
                .build();
    }

    @GetMapping("/{id}/products")
    // PathVariable lấy giá trị nằm trực tiếp trong url, thường là các giá trị định
    // danh duy nhất cho 1 tài nguyên
    public ApiResponse<List<ProductDTO>> showProductsByCategoryId(@PathVariable Long id) {
        List<ProductDTO> products = categoryService.getProductDTOsById(id);
        return ApiResponse.<List<ProductDTO>>builder()
                .status(200)
                .message("OK")
                .data(products)
                .build();
    }

    @GetMapping("/search")
    // RequestParam lấy các giá trị sau dấu hỏi trên url, thường là các giá trị phụ
    // trợ (lọc, sắp xếp, phân trang).
    public ApiResponse<List<CategoryDTO>> searchCategoryByName(
            @RequestParam(required = true) String name) {

        return ApiResponse.<List<CategoryDTO>>builder()
                .status(200)
                .message("OK")
                .data(categoryService.search(name))
                .build();
    }

    @PostMapping
    // Request body là phần dữ liệu chính (payload) mà client gửi lên cho server
    // định dạng phổ biến nhất là JSON
    // Annotation @RequestBody sẽ tự động chuyển JSON Sang object tương ứng bằng thư
    // viện JACKSON (qua ObjectMapper) để phản tuần tự
    // Dùng reflection để quét các field sau đó tạo 1 object mới bằng default
    // Constructor rồi dùng setter để gán giá trị từ json vào
    public void addNewCategory(@RequestBody CategoryDTO category) {
        categoryService.addCategory(category);
        // categoryService.addCategory(Category.builder().name("OOP").build());
    }

    @PutMapping("/{id}")
    public void updateCategoryInfo(@PathVariable Long id, @RequestBody CategoryDTO category) {
        categoryService.updateCategory(id, category);
    }

    @PatchMapping("/{id}") // PATCH chỉ cập nhật 1 trường, nếu chỉ cập nhật trường tên thì code này giống
                           // code của PUT
    public void updateCategoryName(@PathVariable Long id, @RequestBody CategoryDTO category) {
        categoryService.updateCategory(id, category);
    }

    @DeleteMapping("/{id}")
    public void deleteCategoryById(@PathVariable Long id) {
        categoryService.deleteCategory(id);
    }
}
