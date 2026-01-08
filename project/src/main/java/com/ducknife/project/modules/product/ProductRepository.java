package com.ducknife.project.modules.product;

import java.util.List;
import java.util.Optional;

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

        public Optional<Product> findById(Long id) {
                List<Product> products = jdbcTemplate.query(
                                "select * from product where id = ?",
                                (rs, row) -> Product.builder().id(rs.getLong("id"))
                                                .name(rs.getString("name"))
                                                .price(rs.getDouble("price"))
                                                .category_id(rs.getLong("category_id"))
                                                .build(),
                                id);
                return products.stream().findFirst();
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

        public boolean existByName(String name) {
                Integer count = jdbcTemplate.queryForObject(
                        "select count(*) from product where name = ?",
                        Integer.class,
                        name
                );
                return count != null && count > 0;
        }

        public int updateProduct(Long id, ProductDTO product) {
                return jdbcTemplate.update(
                                "update product set name = ? where id = ?",
                                product.getName(),
                                id);
        }

        public void save(ProductDTO product) {
                jdbcTemplate.update(
                                "insert into product (name, price, category_id) values (?, ?, ?)",
                                product.getName(), product.getPrice(), product.getCategory_id());
        }

        public int delete(Long id) {
                return jdbcTemplate.update(
                                "delete from product where id = ?",
                                id);
        }
}
