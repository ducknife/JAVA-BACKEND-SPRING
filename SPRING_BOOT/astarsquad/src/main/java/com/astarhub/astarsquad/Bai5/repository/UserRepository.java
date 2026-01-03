package com.astarhub.astarsquad.Bai5.repository;

import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.astarhub.astarsquad.Bai5.User;

@Repository
public class UserRepository {
    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    public User save(User user) {
        jdbcTemplate.update(
            "insert into user (user_name, user_password) values (?, ?)",
            user.getUserName(), user.getUserPassword()
        ); 
        return user; 
    }

    public List<User> findAll() {
        return jdbcTemplate.query(
            "select * from user",
            (rs, row) -> new User(
                rs.getString("user_name"),
                rs.getString("user_password")
            )   
        );
    }
}

// Truy cập DB, được service gọi, thường đặt tên XxxRepository.java
// Spring tự translation SQLException (lỗi)
