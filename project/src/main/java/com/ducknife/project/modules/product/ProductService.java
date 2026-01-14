package com.ducknife.project.modules.product;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.ducknife.project.common.exception.ResourceNotFoundException;
import com.ducknife.project.config.properties.DataSourceProperties;
import com.ducknife.project.config.properties.ServerProperties;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor // chỉ tạo constructor cho final, phù hợp với constructor injection
public class ProductService {
    // in ra thông tin trong file cấu hình
    private final DataSourceProperties dataSourceProperties;
    private final ServerProperties serverProperties;
    private final ProductRepository productRepository;

    @PostConstruct
    public void showConfig() {
        log.info("-".repeat(10) + "DATA SOURCE CONFIG:" + "-".repeat(10));
        log.info(dataSourceProperties.getUrl());
        log.info(dataSourceProperties.getUsername());
        log.info("-".repeat(10) + "SERVICE CONFIG:" + "-".repeat(10));
        log.info(Integer.toString(serverProperties.getPort()));
    }

    public List<ProductDTO> getProducts() {
        log.info("CONTROLLER: Gọi vào nghiệp vụ lấy danh sách sản phẩm!");
        return productRepository.findAll().stream()
                .map(p -> ProductDTO.builder().name(p.getName()).price(p.getPrice()).category_id(p.getCategory_id())
                        .build())
                .collect(Collectors.toList());
    }

    public ProductDTO getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm!"));
        return ProductDTO.builder()
                .name(product.getName())
                .price(product.getPrice())
                .category_id(product.getCategory_id())
                .build();
    }

    public List<ProductDTO> getProductsByNameAndPrice(String name, double minPrice, double maxPrice) {
        return productRepository.findByNameAndPrice(name, minPrice, maxPrice).stream()
                .map(p -> ProductDTO.builder().name(p.getName()).price(p.getPrice()).category_id(p.getCategory_id())
                        .build())
                .collect(Collectors.toList());
    }

    public void updateProduct(Long id, ProductDTO product) {
        int affectedRows = productRepository.updateProduct(id, product);
        if (affectedRows == 0)
            throw new ResourceNotFoundException("Sản phẩm không tồn tại!");
    }

    public void addProduct(ProductDTO product) { // để tạm để bắt lỗi dup key trong DB
        // if (productRepository.existByName(product.getName())) {
        //     throw new ResourceConflictException("Sản phẩm " + product.getName() + " đã tồn tại!");
        // }
        productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        int affectedRows = productRepository.delete(id);
        if (affectedRows == 0)
            throw new ResourceNotFoundException("Không thể xóa sản phẩm không tồn tại!");
    }
}
