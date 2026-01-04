package com.ducknife.project.modules.product;

import java.util.List;

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

    public List<Product> getProducts() {
        log.info("CONTROLLER: Gọi vào nghiệp vụ lấy danh sách sản phẩm!");
        return productRepository.findAll();
    }
}
