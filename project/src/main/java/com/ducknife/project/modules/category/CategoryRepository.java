package com.ducknife.project.modules.category;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository

public class CategoryRepository {
    private final JdbcTemplate jdbcTemplate;

    public CategoryRepository (JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public List<Category> findAll() {
        return jdbcTemplate.query(
            "select * from category",
            (rs, row) -> Category.builder()
                                .id(rs.getLong("id"))
                                .name(rs.getString("name"))
                                .build()
        );
    }

    public Category findByid(Long id) {
        return jdbcTemplate.queryForObject(
            "select * from category where id = ?",
            (rs, row) -> Category.builder()
                                .id(rs.getLong("id"))
                                .name(rs.getString("name"))
                                .build(),
            id
        );
    }

    public void save(String nameCategory) {
        jdbcTemplate.update(
            "insert into category (name) values(?)",
            nameCategory
        );
    }

    public void updateCategoryById(Long id, String newNameCategory) {
        jdbcTemplate.update(
            "update category set name = ? where id = ?",
            newNameCategory, 
            id
        );
    }

    public void delete(Long id) {
        jdbcTemplate.update(
            "delete from category where id = ?",
            id
        );
    }
}
