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
            (rs, row) -> new Category(rs.getLong("id"), rs.getString("name"))
        );
    }
}
