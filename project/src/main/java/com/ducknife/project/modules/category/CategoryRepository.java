package com.ducknife.project.modules.category;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long>{

        Boolean existsByName(String name);

        List<Category> findByName(String name);

        @Query("SELECT c FROM Category c ORDER BY c.name DESC LIMIT 3")
        List<Category> findTop3ByOrderByName();

        // @Query("SELECT c FROM Category c LEFT JOIN FETCH c.products")
        // List<Category> findProductsByCategory();
}
