package com.ducknife.project.modules.product;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.config.properties.DataSourceProperties;
import com.ducknife.project.config.properties.ServerProperties;
import com.ducknife.project.modules.auditlog.AuditLog;
import com.ducknife.project.modules.auditlog.AuditService;
import com.ducknife.project.modules.category.Category;
import com.ducknife.project.modules.category.CategoryRepository;
import com.ducknife.project.modules.product.dto.ProductRequest;
import com.ducknife.project.modules.product.dto.ProductResponse;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor // chỉ tạo constructor cho final, phù hợp với constructor injection
@Transactional(readOnly = true)
public class ProductService {
    // in ra thông tin trong file cấu hình
    private final DataSourceProperties dataSourceProperties;
    private final ServerProperties serverProperties;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final AuditService auditService;

    @PostConstruct
    public void showConfig() {
        log.info("-".repeat(10) + "DATA SOURCE CONFIG:" + "-".repeat(10));
        log.info(dataSourceProperties.getUrl());
        log.info(dataSourceProperties.getUsername());
        log.info("-".repeat(10) + "SERVICE CONFIG:" + "-".repeat(10));
        log.info(Integer.toString(serverProperties.getPort()));
    }

    public List<ProductResponse> getProducts() {
        log.info("CONTROLLER: Gọi vào nghiệp vụ lấy danh sách sản phẩm!");
        return productRepository.findAll()
                .stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm!"));
        return ProductResponse.from(product);
    }

    public List<ProductResponse> getProductsByNameAndPrice(String name, double minPrice, double maxPrice) {
        return productRepository.findByNameAndPrice(name, minPrice, maxPrice).stream()
                .map(ProductResponse::from)
                .collect(Collectors.toList());
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'COLLABORATOR')")
    public ProductResponse updateProduct(Long id, ProductRequest product) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Không tìm thấy sản phẩm!");
        }
        Product productInDB = productRepository.findById(id).get();
        productInDB.setName(product.getName());
        productInDB.setPrice(product.getPrice());
        Category category = categoryRepository.findById(product.getCategory_id())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục sản phẩm"));
        productInDB.setCategory(category);
        Product savedProduct = productRepository.save(productInDB);
        return ProductResponse.from(savedProduct);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'COLLABORATOR')")
    public ProductResponse addProduct(ProductRequest product) { // để tạm để bắt lỗi dup key trong DB
        // if (productRepository.existByName(product.getName())) {
        // throw new ResourceConflictException("Sản phẩm " + product.getName() + " đã
        // tồn tại!");
        // }
        Category category = categoryRepository.findById(product.getCategory_id())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy danh mục sản phẩm!"));
        Product newProduct = Product.builder()
                .name(product.getName())
                .price(product.getPrice())
                .category(category)
                .build();
        Product savedProduct = productRepository.save(newProduct);
        auditService.add(AuditLog.builder()
                .logType("SERVER")
                .logMessage("THÊM MỚI THÀNH CÔNG SẢN PHẨM")
                .build());
        // throw new ResourceNotFoundException("NO"); // dù cha bị lỗi thì audit log add có transaction là required_new vẫn sống.
        return ProductResponse.from(savedProduct);
    }

    @Transactional
    @PreAuthorize("hasAnyRole('ADMIN', 'COLLABORATOR')")
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sản phẩm không tồn tại!");
        }
        productRepository.deleteById(id);
    }
}
