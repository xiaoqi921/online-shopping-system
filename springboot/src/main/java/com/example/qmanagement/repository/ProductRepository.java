package com.example.qmanagement.repository;

import com.example.qmanagement.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // 按名称模糊搜索
    List<Product> findByNameContainingIgnoreCase(String name);

    // 按分类筛选
    List<Product> findByCategory(String category);

    // 按名称和分类联合搜索
    @Query("SELECT p FROM Product p WHERE " +
           "(:name IS NULL OR p.name LIKE %:name%) AND " +
           "(:category IS NULL OR p.category = :category)")
    List<Product> searchProducts(@Param("name") String name, @Param("category") String category);
}
