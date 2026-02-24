package com.ducknife.project.modules.category;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ducknife.project.common.exception.ResourceConflictException;
import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.modules.auditlog.AuditLog;
import com.ducknife.project.modules.auditlog.AuditService;
import com.ducknife.project.modules.product.ProductRepository;
import com.ducknife.project.modules.product.dto.ProductResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // mặc định tất cả là chỉ đọc, nếu cần chính sửa, thêm thì thêm @Transactional
                                // riêng.
public class CategoryService {

    private final AuditService auditService;
    public final CategoryRepository categoryRepository;
    public final ProductRepository productRepository;

    public List<CategoryDTO> getCategories() {
        // CategoryDTO res = this.getCategoryById(1L); <= self-invocation xảy ra do nó
        // tự gọi 1 hàm của đối tượng thật, ko phải proxy do Application Context dùng
        // CGLIB tạo ra;
        // khi này Transactional bị vô hiệu hóa, hàm được gọi sẽ chỉ là 1 hàm thuần túy
        // mà không có mở transaction trong proxy;
        // Lưu ý: proxy gồm đối tượng tham chiếu đến đối tượng thật, các lệnh try -
        // catch mở giao dịch.
        // Cách tốt nhất là tách biệt Service, ví dụ gửi mail thì phải để trong
        // MailService, không nên để trong người dùng.
        return categoryRepository.findAll().stream()
                .map(CategoryDTO::from)
                .collect(Collectors.toList());
    }

    // hàm get không cần transactional vì mặc định đã có
    public CategoryDTO getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục sản phẩm!"));
        // ko cần save do đang managed nếu dùng JPA/hibernate;
        // category.setName("Test Persist of Entity"); <- Test Persist/Managed của
        // entity;
        // còn JdcbTemplate ko có dirty checking
        return CategoryDTO.from(category);
    }
    // hết hàm này object category kia chuyển về detached(), khi đó mọi thay đổi
    // không ảnh hưởng đến DB.

    public List<ProductResponse> getProductsByCategoryId(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục sản phẩm!"));

        return productRepository.findByCategoryId(id).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());

    }

    public List<CategoryDTO> searchByName(String name) {
        return categoryRepository.findByName(name).stream()
                .map(CategoryDTO::from)
                .collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class, timeout = 10) // transaction là giao dịch quản lí những thay đổi thực
                                                                // hiện trong hệ thống, mục tiêu chính là cung cấp các
                                                                // đặc điểm ACID đảm bảo nhất quán và hợp lệ dữ liệu
    public void addCategory(CategoryDTO categoryDTO) {
        if (categoryRepository.existsByName(categoryDTO.getName())) {
            throw new ResourceConflictException(
                    "Danh mục sản phẩm " + categoryDTO.getName() + " đã tồn tại! (Bắt khi service thấy)");
        }
        auditService.add(AuditLog.builder().logType("SERVER").logMessage("THÊM MỚI THÀNH CÔNG DANH MỤC SẢN PHẨM!").build());
        Category newCategory = Category.builder()
                .name(categoryDTO.getName())
                .build();
        categoryRepository.save(newCategory);
    }

    @Transactional(rollbackFor = Exception.class, timeout = 10)
    public void updateCategory(Long id, CategoryDTO categoryDTO) {
        if (!categoryRepository.existsById(id))
            throw new ResourceNotFoundException("Danh mục sản phẩm không tồn tại!");
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Danh mục sản phẩm không tồn tại!"));
        category.setName(categoryDTO.getName());
        categoryRepository.save(category);
    }

    @Transactional(rollbackFor = Exception.class, timeout = 10)
    public void deleteCategory(Long id) {
        if (!categoryRepository.existsById(id))
            throw new ResourceNotFoundException("Không thể xóa danh mục sản phẩm không tồn tại!");
        categoryRepository.deleteById(id);
    }

}