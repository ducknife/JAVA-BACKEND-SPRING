package com.ducknife.project.modules.product;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

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
        System.out.println("-".repeat(10) + "DATA SOURCE CONFIG:" + "-".repeat(10));
        System.out.println(dataSourceProperties.getUrl());
        System.out.println(dataSourceProperties.getUsername());
        System.out.println(dataSourceProperties.getPassword());
        System.out.println("-".repeat(10) + "SERVICE CONFIG:" + "-".repeat(10));
        System.out.println(serverProperties.getPort());
    }

    public List<ProductDTO> getProductDTOs() {
        log.info("CONTROLLER: Gọi vào nghiệp vụ lấy danh sách sản phẩm!");
        return productRepository.findAll().stream()
                .map(p -> ProductDTO.builder().name(p.getName()).price(p.getPrice()).category_id(p.getCategory_id())
                        .build())
                .collect(Collectors.toList());
    }

    public List<ProductDTO> getProductDTOsByNameAndPrice(String name, double minPrice, double maxPrice) {
        return productRepository.findByNameAndPrice(name, minPrice, maxPrice).stream()
                .map(p -> ProductDTO.builder().name(p.getName()).price(p.getPrice()).category_id(p.getCategory_id())
                        .build())
                .collect(Collectors.toList());
    }

    public ProductDTO updateProduct(Long id, ProductDTO product) {
        Product updatedProduct = productRepository.updateProduct(id, product);
        return ProductDTO.builder()
                .name(updatedProduct.getName())
                .price(updatedProduct.getPrice())
                .category_id(updatedProduct.getCategory_id())
                .build();
    }
}
