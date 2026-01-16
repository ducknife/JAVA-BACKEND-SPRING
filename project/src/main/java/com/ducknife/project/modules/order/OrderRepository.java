package com.ducknife.project.modules.order;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

// @Repository // nếu đã extends jpa thì không cần thêm annotation này 
// JpaRepository kế thừa từ Repository, CrudRepository, PagingAndSortingRepository
// 
public interface OrderRepository extends JpaRepository<Order, Long>{
    List<Order> findByUserId(Long userId);
    Void deleteByUserId(Long userId);
    @Query("SELECT o FROM Order o JOIN FETCH o.user")
    List<Order> findOrdersByUser();
}