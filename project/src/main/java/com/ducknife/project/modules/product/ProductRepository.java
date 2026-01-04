package com.ducknife.project.modules.product;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Repository
@Slf4j
@RequiredArgsConstructor
public class ProductRepository {
    private final JdbcTemplate jdbcTemplate;
    
    public List<Product> findAll() {
        log.info("SERVICE: Muốn lấy danh sách sản phẩm trong CSDL");
        return jdbcTemplate.query(
            "select * from product",
            (rs, row) -> Product.builder().id(rs.getLong("id"))
            .name(rs.getString("name"))
            .price(rs.getDouble("price"))
            .build());
    }
}
