package com.ducknife.project.modules.orderdetail;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {
    @EntityGraph(attributePaths = {"product", "order"})
    List<OrderDetail> findAll();
}
