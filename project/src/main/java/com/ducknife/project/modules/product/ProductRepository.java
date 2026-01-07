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
                                                .category_id(rs.getLong("category_id"))
                                                .build());
        }

        public Product findById(Long id) {
                return jdbcTemplate.queryForObject(
                                "select * from product where id = ?",
                                (rs, row) -> Product.builder().id(rs.getLong("id"))
                                                .name(rs.getString("name"))
                                                .price(rs.getDouble("price"))
                                                .category_id(rs.getLong("category_id"))
                                                .build(),
                                id);
        }

        public List<Product> findByNameAndPrice(String name, double minPrice, double maxPrice) {
                return jdbcTemplate.query(
                                "select * from product where name = ? and price >= ? and price <= ?",
                                (rs, row) -> Product.builder().id(rs.getLong("id"))
                                                .name(rs.getString("name"))
                                                .price(rs.getDouble("price"))
                                                .category_id(rs.getLong("category_id"))
                                                .build(),
                                name,
                                minPrice,
                                maxPrice);
        }

        public Product updateProduct(Long id, ProductDTO product) {
                Product updatedProduct = Product.builder()
                                .id(id)
                                .name(product.getName())
                                .price(product.getPrice())
                                .category_id(product.getCategory_id())
                                .build();
                jdbcTemplate.update(
                                "update product set name = ? where id = ?",
                                product.getName(),
                                id);
                return updatedProduct;
        }

        public void save(ProductDTO product) {
                jdbcTemplate.update(
                                "insert into product (name, price, category_id) values (?, ?, ?)",
                                product.getName(), product.getPrice(), product.getCategory_id());
        }
}
