package com.ducknife.project.modules.user;

import java.util.List;
import java.util.Optional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import com.ducknife.project.modules.order.Order;

import lombok.RequiredArgsConstructor;

@Repository
@RequiredArgsConstructor
public class UserRepository {
        
    private final JdbcTemplate jdbcTemplate;

    public List<User> findAll() {
        return jdbcTemplate.query(
                "select * from user",
                (rs, row) -> User.builder()
                        .id(rs.getLong("user_id"))
                        .fullName(rs.getString("full_name"))
                        .userName(rs.getString("user_name"))
                        .password(rs.getString("password"))
                        .build());
    }

    public Optional<User> findById(Long id) {
        List<User> users = jdbcTemplate.query(
                "select * from user where user_id = ?",
                (rs, row) -> User.builder()
                        .id(rs.getLong("user_id"))
                        .fullName(rs.getString("full_name"))
                        .userName(rs.getString("user_name"))
                        .password(rs.getString("password"))
                        .build(),
                id);
        return users.stream().findFirst();
    }

    public List<Order> findOrdersById(Long id) {
        User user = jdbcTemplate.queryForObject(
                "select * from user where user_id = ?",
                (rs, row) -> User.builder()
                        .id(rs.getLong("user_id"))
                        .fullName(rs.getString("full_name"))
                        .userName(rs.getString("user_name"))
                        .password(rs.getString("password"))
                        .build(),
                id);
        return jdbcTemplate.query(
                "select * from sale_order where user_id = ?",
                (rs, row) -> Order.builder()
                        .id(rs.getLong("order_id"))
                        .user(user)
                        .build(),
                id);
    }
}
