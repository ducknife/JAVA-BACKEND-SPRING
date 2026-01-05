package com.ducknife.project.modules.category;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ducknife.project.modules.product.Product;

@Repository

public class CategoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Category> findAll() {
        return jdbcTemplate.query(
                "select * from category",
                (rs, row) -> Category.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build());
    }

    public Category findById(Long id) {
        return jdbcTemplate.queryForObject(
                "select * from category where id = ?",
                (rs, row) -> Category.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build(),
                id);
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

    public void updateCategoryById(Long id, CategoryDTO categoryDTO) {
        jdbcTemplate.update(
                "update category set name = ? where id = ?",
                categoryDTO.getName(),
                id);
    }

    public void delete(Long id) {
        jdbcTemplate.update(
                "delete from category where id = ?",
                id);
    }
}
