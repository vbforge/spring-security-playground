package com.vbforge.security.restbasic.service.impl;

import com.vbforge.security.restbasic.dto.ProductDTO;
import com.vbforge.security.restbasic.entity.Product;
import com.vbforge.security.restbasic.entity.Tag;
import com.vbforge.security.restbasic.exception.DuplicateResourceException;
import com.vbforge.security.restbasic.exception.ResourceNotFoundException;
import com.vbforge.security.restbasic.mapper.ProductMapper;
import com.vbforge.security.restbasic.repository.ProductRepository;
import com.vbforge.security.restbasic.repository.TagRepository;
import com.vbforge.security.restbasic.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Implementation of ProductService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final TagRepository tagRepository;
    private final ProductMapper productMapper;

    @Override
    @Transactional
    public ProductDTO createProduct(ProductDTO productDTO) {
        log.info("Creating new product: {}", productDTO.getName());

        // Check if product with same name already exists
        if (productRepository.existsByName(productDTO.getName())) {
            throw new DuplicateResourceException("Product", "name", productDTO.getName());
        }

        Product product = productMapper.toEntity(productDTO);
        Product savedProduct = productRepository.save(product);

        log.info("Product created successfully with id: {}", savedProduct.getId());
        return productMapper.toDTO(savedProduct);
    }

    @Override
    public ProductDTO getProductById(Long id) {
        log.info("Fetching product with id: {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        return productMapper.toDTO(product);
    }

    @Override
    public List<ProductDTO> getAllProducts() {
        log.info("Fetching all products");

        List<Product> products = productRepository.findAll();
        return productMapper.toDTOList(products);
    }

    @Override
    @Transactional
    public ProductDTO updateProduct(Long id, ProductDTO productDTO) {
        log.info("Updating product with id: {}", id);

        Product existingProduct = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Product", id));

        // Check if updating name to an existing name
        if (productDTO.getName() != null &&
                !existingProduct.getName().equals(productDTO.getName()) &&
                productRepository.existsByName(productDTO.getName())) {
            throw new DuplicateResourceException("Product", "name", productDTO.getName());
        }

        productMapper.updateEntityFromDTO(productDTO, existingProduct);
        Product updatedProduct = productRepository.save(existingProduct);

        log.info("Product updated successfully with id: {}", id);
        return productMapper.toDTO(updatedProduct);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        log.info("Deleting product with id: {}", id);

        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product", id);
        }

        productRepository.deleteById(id);
        log.info("Product deleted successfully with id: {}", id);
    }

    @Override
    public List<ProductDTO> searchProductsByName(String name) {
        log.info("Searching products by name: {}", name);

        List<Product> products = productRepository.findByNameContainingIgnoreCase(name);
        return productMapper.toDTOList(products);
    }

    @Override
    public List<ProductDTO> findProductsByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.info("Finding products between price range: {} - {}", minPrice, maxPrice);

        List<Product> products = productRepository.findByPriceBetween(minPrice, maxPrice);
        return productMapper.toDTOList(products);
    }

    @Override
    public List<ProductDTO> findProductsByTagName(String tagName) {
        log.info("Finding products with tag: {}", tagName);

        List<Product> products = productRepository.findByTagName(tagName);
        return productMapper.toDTOList(products);
    }

    @Override
    @Transactional
    public ProductDTO addTagToProduct(Long productId, Long tagId) {
        log.info("Adding tag {} to product {}", tagId, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", tagId));

        product.addTag(tag);
        Product savedProduct = productRepository.save(product);

        log.info("Tag added successfully to product");
        return productMapper.toDTO(savedProduct);
    }

    @Override
    @Transactional
    public ProductDTO removeTagFromProduct(Long productId, Long tagId) {
        log.info("Removing tag {} from product {}", tagId, productId);

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product", productId));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", tagId));

        product.removeTag(tag);
        Product savedProduct = productRepository.save(product);

        log.info("Tag removed successfully from product");
        return productMapper.toDTO(savedProduct);
    }
}
