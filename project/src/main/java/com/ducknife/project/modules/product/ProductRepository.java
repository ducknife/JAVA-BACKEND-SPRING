package com.ducknife.project.modules.product;

import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

// findByNameAndPrice(): name = ? and price >= and <= , existsByName(), updateProduct(), 
public interface ProductRepository extends JpaRepository<Product, Long>{

        @Query("SELECT p FROM Product p WHERE p.name = :name and p.price >= :minPrice and p.price <= :maxPrice")
        @EntityGraph(attributePaths = "category")
        List<Product> findByNameAndPrice(@Param("name") String name, @Param("minPrice") double minPrice, @Param("maxPrice") double maxPrice);
        Boolean existsByName(String name);

        @EntityGraph(attributePaths = "category")
        List<Product> findByCategoryId(Long categoryId);
        
        // @Query("SELECT p FROM Product p JOIN FETCH p.category")
        @EntityGraph(attributePaths = "category")
        List<Product> findAll();

}
