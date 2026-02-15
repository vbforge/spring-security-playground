package com.vbforge.security.restbasic.repository;

import com.vbforge.security.restbasic.entity.Product;
import com.vbforge.security.restbasic.entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for ProductRepository.
 *
 * @DataJpaTest provides:
 * - H2 in-memory database
 * - Auto-configuration for JPA
 * - Transaction rollback after each test
 */
@DataJpaTest
@ActiveProfiles("test")
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private TagRepository tagRepository;

    private Product product1;
    private Product product2;
    private Tag tag1;

    @BeforeEach
    void setUp() {
        // Create test products
        product1 = Product.builder()
                .name("Laptop")
                .description("Gaming laptop")
                .price(new BigDecimal("1299.99"))
                .build();

        product2 = Product.builder()
                .name("Mouse")
                .description("Wireless mouse")
                .price(new BigDecimal("29.99"))
                .build();

        tag1 = Tag.builder()
                .name("Electronics")
                .build();

        tagRepository.save(tag1);
        product1.addTag(tag1);

        productRepository.save(product1);
        productRepository.save(product2);
    }

    @Test
    void shouldSaveProduct() {
        Product newProduct = Product.builder()
                .name("Keyboard")
                .description("Mechanical keyboard")
                .price(new BigDecimal("89.99"))
                .build();

        Product saved = productRepository.save(newProduct);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Keyboard");
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void shouldFindProductById() {
        Optional<Product> found = productRepository.findById(product1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Laptop");
    }

    @Test
    void shouldFindAllProducts() {
        List<Product> products = productRepository.findAll();

        assertThat(products).hasSize(2);
        assertThat(products).extracting(Product::getName)
                .containsExactlyInAnyOrder("Laptop", "Mouse");
    }

    @Test
    void shouldFindByNameContainingIgnoreCase() {
        List<Product> products = productRepository.findByNameContainingIgnoreCase("lap");

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    void shouldFindByPriceBetween() {
        List<Product> products = productRepository.findByPriceBetween(
                new BigDecimal("20.00"),
                new BigDecimal("100.00")
        );

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Mouse");
    }

    @Test
    void shouldFindByName() {
        Optional<Product> found = productRepository.findByName("Laptop");

        assertThat(found).isPresent();
        assertThat(found.get().getDescription()).isEqualTo("Gaming laptop");
    }

    @Test
    void shouldCheckExistsByName() {
        assertThat(productRepository.existsByName("Laptop")).isTrue();
        assertThat(productRepository.existsByName("NonExistent")).isFalse();
    }

    @Test
    void shouldFindByTagName() {
        List<Product> products = productRepository.findByTagName("Electronics");

        assertThat(products).hasSize(1);
        assertThat(products.get(0).getName()).isEqualTo("Laptop");
    }

    @Test
    void shouldDeleteProduct() {
        productRepository.deleteById(product1.getId());

        Optional<Product> deleted = productRepository.findById(product1.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    void shouldUpdateProduct() {
        Product product = productRepository.findById(product1.getId()).orElseThrow();
        product.setPrice(new BigDecimal("1499.99"));

        Product updated = productRepository.save(product);

        assertThat(updated.getPrice()).isEqualByComparingTo(new BigDecimal("1499.99"));
    }
}