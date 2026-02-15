package com.vbforge.security.restjwt.service;

import com.vbforge.security.restjwt.dto.ProductDTO;
import com.vbforge.security.restjwt.entity.Product;
import com.vbforge.security.restjwt.entity.Tag;
import com.vbforge.security.restjwt.exception.DuplicateResourceException;
import com.vbforge.security.restjwt.exception.ResourceNotFoundException;
import com.vbforge.security.restjwt.mapper.ProductMapper;
import com.vbforge.security.restjwt.repository.ProductRepository;
import com.vbforge.security.restjwt.repository.TagRepository;
import com.vbforge.security.restjwt.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Service tests for ProductService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private TagRepository tagRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;
    private Tag tag;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1299.99"))
                .build();

        productDTO = ProductDTO.builder()
                .id(1L)
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1299.99"))
                .build();

        tag = Tag.builder()
                .id(1L)
                .name("Electronics")
                .build();
    }

    @Test
    void createProduct_ShouldReturnCreatedProduct() {
        when(productRepository.existsByName(productDTO.getName())).thenReturn(false);
        when(productMapper.toEntity(productDTO)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        ProductDTO result = productService.createProduct(productDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Laptop");
        verify(productRepository).save(product);
    }

    @Test
    void createProduct_WhenNameExists_ShouldThrowDuplicateResourceException() {
        when(productRepository.existsByName(productDTO.getName())).thenReturn(true);

        assertThatThrownBy(() -> productService.createProduct(productDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Product already exists with name");

        verify(productRepository, never()).save(any());
    }

    @Test
    void getProductById_ShouldReturnProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        ProductDTO result = productService.getProductById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(productRepository).findById(1L);
    }

    @Test
    void getProductById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found with id: 999");
    }

    @Test
    void getAllProducts_ShouldReturnProductList() {
        List<Product> products = Arrays.asList(product);
        List<ProductDTO> productDTOs = Arrays.asList(productDTO);

        when(productRepository.findAll()).thenReturn(products);
        when(productMapper.toDTOList(products)).thenReturn(productDTOs);

        List<ProductDTO> result = productService.getAllProducts();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    void updateProduct_ShouldReturnUpdatedProduct() {
        ProductDTO updateDTO = ProductDTO.builder()
                .name("Laptop")
                .price(new BigDecimal("1499.99"))
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        ProductDTO result = productService.updateProduct(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(productMapper).updateEntityFromDTO(updateDTO, product);
        verify(productRepository).save(product);
    }

    @Test
    void deleteProduct_ShouldDeleteProduct() {
        when(productRepository.existsById(1L)).thenReturn(true);

        productService.deleteProduct(1L);

        verify(productRepository).deleteById(1L);
    }

    @Test
    void deleteProduct_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(productRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    void searchProductsByName_ShouldReturnMatchingProducts() {
        List<Product> products = Arrays.asList(product);
        List<ProductDTO> productDTOs = Arrays.asList(productDTO);

        when(productRepository.findByNameContainingIgnoreCase("Lap")).thenReturn(products);
        when(productMapper.toDTOList(products)).thenReturn(productDTOs);

        List<ProductDTO> result = productService.searchProductsByName("Lap");

        assertThat(result).hasSize(1);
        verify(productRepository).findByNameContainingIgnoreCase("Lap");
    }

    @Test
    void addTagToProduct_ShouldAddTagSuccessfully() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        ProductDTO result = productService.addTagToProduct(1L, 1L);

        assertThat(result).isNotNull();
        verify(productRepository).save(product);
    }

    @Test
    void removeTagFromProduct_ShouldRemoveTagSuccessfully() {
        product.addTag(tag);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(productRepository.save(product)).thenReturn(product);
        when(productMapper.toDTO(product)).thenReturn(productDTO);

        ProductDTO result = productService.removeTagFromProduct(1L, 1L);

        assertThat(result).isNotNull();
        verify(productRepository).save(product);
    }
}