package com.vbforge.security.oauth2.repository;

import com.vbforge.security.oauth2.entity.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository tests for TagRepository.
 */
@DataJpaTest
@ActiveProfiles("test")
class TagRepositoryTest {

    @Autowired
    private TagRepository tagRepository;

    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        tag1 = Tag.builder()
                .name("Electronics")
                .build();

        tag2 = Tag.builder()
                .name("Gaming")
                .build();

        tagRepository.save(tag1);
        tagRepository.save(tag2);
    }

    @Test
    void shouldSaveTag() {
        Tag newTag = Tag.builder()
                .name("Accessories")
                .build();

        Tag saved = tagRepository.save(newTag);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("Accessories");
    }

    @Test
    void shouldFindTagById() {
        Optional<Tag> found = tagRepository.findById(tag1.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Electronics");
    }

    @Test
    void shouldFindAllTags() {
        List<Tag> tags = tagRepository.findAll();

        assertThat(tags).hasSize(2);
        assertThat(tags).extracting(Tag::getName)
                .containsExactlyInAnyOrder("Electronics", "Gaming");
    }

    @Test
    void shouldFindByName() {
        Optional<Tag> found = tagRepository.findByName("Gaming");

        assertThat(found).isPresent();
        assertThat(found.get().getId()).isNotNull();
    }

    @Test
    void shouldCheckExistsByName() {
        assertThat(tagRepository.existsByName("Electronics")).isTrue();
        assertThat(tagRepository.existsByName("NonExistent")).isFalse();
    }

    @Test
    void shouldDeleteTag() {
        tagRepository.deleteById(tag1.getId());

        Optional<Tag> deleted = tagRepository.findById(tag1.getId());
        assertThat(deleted).isEmpty();
    }

    @Test
    void shouldUpdateTag() {
        Tag tag = tagRepository.findById(tag1.getId()).orElseThrow();
        tag.setName("Updated Electronics");

        Tag updated = tagRepository.save(tag);

        assertThat(updated.getName()).isEqualTo("Updated Electronics");
    }
}