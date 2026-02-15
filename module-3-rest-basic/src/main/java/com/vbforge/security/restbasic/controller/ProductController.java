package com.vbforge.security.restbasic.controller;

import com.vbforge.security.restbasic.dto.ProductDTO;
import com.vbforge.security.restbasic.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST Controller for Product operations.
 *
 * All endpoints are protected by Spring Security default configuration.
 */
@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController {

    private final ProductService productService;

    /**
     * Create a new product
     * POST /api/products
     */
    @PostMapping
    public ResponseEntity<ProductDTO> createProduct(@Valid @RequestBody ProductDTO productDTO) {
        log.info("REST request to create product: {}", productDTO.getName());
        ProductDTO createdProduct = productService.createProduct(productDTO);
        return new ResponseEntity<>(createdProduct, HttpStatus.CREATED);
    }

    /**
     * Get all products
     * GET /api/products
     */
    @GetMapping
    public ResponseEntity<List<ProductDTO>> getAllProducts() {
        log.info("REST request to get all products");
        List<ProductDTO> products = productService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    /**
     * Get product by ID
     * GET /api/products/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(@PathVariable Long id) {
        log.info("REST request to get product with id: {}", id);
        ProductDTO product = productService.getProductById(id);
        return ResponseEntity.ok(product);
    }

    /**
     * Update product
     * PUT /api/products/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @PathVariable Long id,
            @Valid @RequestBody ProductDTO productDTO) {
        log.info("REST request to update product with id: {}", id);
        ProductDTO updatedProduct = productService.updateProduct(id, productDTO);
        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * Delete product
     * DELETE /api/products/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        log.info("REST request to delete product with id: {}", id);
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Search products by name
     * GET /api/products/search?name=xxx
     */
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(@RequestParam String name) {
        log.info("REST request to search products by name: {}", name);
        List<ProductDTO> products = productService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    /**
     * Find products by price range
     * GET /api/products/price-range?min=10&max=100
     */
    @GetMapping("/price-range")
    public ResponseEntity<List<ProductDTO>> findByPriceRange(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        log.info("REST request to find products in price range: {} - {}", min, max);
        List<ProductDTO> products = productService.findProductsByPriceRange(min, max);
        return ResponseEntity.ok(products);
    }

    /**
     * Find products by tag name
     * GET /api/products/by-tag?tagName=xxx
     */
    @GetMapping("/by-tag")
    public ResponseEntity<List<ProductDTO>> findByTagName(@RequestParam String tagName) {
        log.info("REST request to find products by tag: {}", tagName);
        List<ProductDTO> products = productService.findProductsByTagName(tagName);
        return ResponseEntity.ok(products);
    }

    /**
     * Add tag to product
     * POST /api/products/{productId}/tags/{tagId}
     */
    @PostMapping("/{productId}/tags/{tagId}")
    public ResponseEntity<ProductDTO> addTagToProduct(
            @PathVariable Long productId,
            @PathVariable Long tagId) {
        log.info("REST request to add tag {} to product {}", tagId, productId);
        ProductDTO product = productService.addTagToProduct(productId, tagId);
        return ResponseEntity.ok(product);
    }

    /**
     * Remove tag from product
     * DELETE /api/products/{productId}/tags/{tagId}
     */
    @DeleteMapping("/{productId}/tags/{tagId}")
    public ResponseEntity<Void> removeTagFromProduct(
            @PathVariable Long productId,
            @PathVariable Long tagId) {
        log.info("REST request to remove tag {} from product {}", tagId, productId);
        productService.removeTagFromProduct(productId, tagId);
        return ResponseEntity.noContent().build();
    }
}