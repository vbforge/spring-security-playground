package com.vbforge.security.oauth2.repository;

import com.vbforge.security.oauth2.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Product entity.
 * 
 * Provides CRUD operations and custom queries.
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    /**
     * Find products by name (case-insensitive, partial match)
     */
    List<Product> findByNameContainingIgnoreCase(String name);

    /**
     * Find products by price range
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find product by exact name
     */
    Optional<Product> findByName(String name);

    /**
     * Check if product exists by name
     */
    boolean existsByName(String name);

    /**
     * Find products by tag name
     */
    @Query("SELECT DISTINCT p FROM Product p JOIN p.tags t WHERE t.name = :tagName")
    List<Product> findByTagName(String tagName);
}