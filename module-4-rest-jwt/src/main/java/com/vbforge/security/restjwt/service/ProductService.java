package com.vbforge.security.restjwt.service;

import com.vbforge.security.restjwt.dto.ProductDTO;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface for Product operations.
 */
public interface ProductService {

    /**
     * Create a new product
     */
    ProductDTO createProduct(ProductDTO productDTO);

    /**
     * Get product by ID
     */
    ProductDTO getProductById(Long id);

    /**
     * Get all products
     */
    List<ProductDTO> getAllProducts();

    /**
     * Update existing product
     */
    ProductDTO updateProduct(Long id, ProductDTO productDTO);

    /**
     * Delete product by ID
     */
    void deleteProduct(Long id);

    /**
     * Search products by name
     */
    List<ProductDTO> searchProductsByName(String name);

    /**
     * Find products by price range
     */
    List<ProductDTO> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * Find products by tag name
     */
    List<ProductDTO> findProductsByTagName(String tagName);

    /**
     * Add tag to product
     */
    ProductDTO addTagToProduct(Long productId, Long tagId);

    /**
     * Remove tag from product
     */
    ProductDTO removeTagFromProduct(Long productId, Long tagId);
}
