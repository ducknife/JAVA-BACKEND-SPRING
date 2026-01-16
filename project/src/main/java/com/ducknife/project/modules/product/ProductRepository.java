package com.ducknife.project.modules.product;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
// findByNameAndPrice(): name = ? and price >= and <= , existsByName(), updateProduct(), 
public interface ProductRepository extends JpaRepository<Product, Long>{

        @Query("SELECT p FROM Product p WHERE p.name = ?1 and p.price >= ?2 and p.price <= ?3")
        List<Product> findByNameAndPrice(String name, double minPrice, double maxPrice);
        Boolean existsByName(String name);

        List<Product> findByCategoryId(Long categoryId);

}
