package com.vbforge.security.oauth2.repository;

import com.vbforge.security.oauth2.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for Tag entity.
 * 
 * Provides CRUD operations and custom queries.
 */
@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    /**
     * Find tag by exact name
     */
    Optional<Tag> findByName(String name);

    /**
     * Check if tag exists by name
     */
    boolean existsByName(String name);
}