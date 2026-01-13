package com.ducknife.project.modules.category;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ducknife.project.modules.product.Product;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class CategoryRepository {
        private final JdbcTemplate jdbcTemplate;

        public List<Category> findAll() {
                return jdbcTemplate.query(
                                "select * from category",
                                (rs, row) -> Category.builder()
                                                .id(rs.getLong("id"))
                                                .name(rs.getString("name"))
                                                .build());
        }

        public Optional<Category> findById(Long id) {
                List<Category> categories = jdbcTemplate.query(
                                "select * from category where id = ?",
                                (rs, row) -> Category.builder()
                                                .id(rs.getLong("id"))
                                                .name(rs.getString("name"))
                                                .build(),
                                id);
                return categories.stream().findFirst();
        }

        public boolean existsByName(String name) {
                Integer count = jdbcTemplate.queryForObject(
                                "select count(*) from category where name = ?",
                                Integer.class,
                                name);
                return count != null && count > 0;
        }

        public List<Category> findByName(String name) {
                return jdbcTemplate.query(
                                "select * from category where name = ?",
                                (rs, row) -> Category.builder()
                                                .id(rs.getLong("id"))
                                                .name(rs.getString("name"))
                                                .build(),
                                name);
        }

        public List<Product> findProductsById(Long id) {
                return jdbcTemplate.query(
                                "select * from product where category_id = ?",
                                (rs, row) -> Product.builder()
                                                .id(rs.getLong("id"))
                                                .name(rs.getString("name"))
                                                .price(rs.getDouble("price"))
                                                .category_id(rs.getLong("category_id"))
                                                .build(),
                                id);
        }

        public void save(CategoryDTO categoryDTO) {
                jdbcTemplate.update(
                                "insert into category (name) values(?)",
                                categoryDTO.getName());
        }

        public int updateCategoryById(Long id, CategoryDTO categoryDTO) {
                return jdbcTemplate.update( // trả về số dòng ảnh hưởng
                                "update category set name = ? where id = ?",
                                categoryDTO.getName(),
                                id);
        }

        public int delete(Long id) {
                return jdbcTemplate.update(
                                "delete from category where id = ?",
                                id);
        }
}
