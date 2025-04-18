package com.cordestitch.repository.product;

import com.cordestitch.entity.product.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    Optional<Category> findByCategoryName(String categoryName);

    Optional<Category> findByCategoryId(String categoryId);
}
