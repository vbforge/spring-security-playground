package com.vbforge.security.restjwt.controller;

import com.vbforge.security.restjwt.dto.TagDTO;
import com.vbforge.security.restjwt.service.TagService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Tag operations.
 *
 * All endpoints are protected by Spring Security default configuration.
 */
@RestController
@RequestMapping("/api/tags")
@RequiredArgsConstructor
@Slf4j
public class TagController {

    private final TagService tagService;

    /**
     * Create a new tag
     * POST /api/tags
     */
    @PostMapping
    public ResponseEntity<TagDTO> createTag(@Valid @RequestBody TagDTO tagDTO) {
        log.info("REST request to create tag: {}", tagDTO.getName());
        TagDTO createdTag = tagService.createTag(tagDTO);
        return new ResponseEntity<>(createdTag, HttpStatus.CREATED);
    }

    /**
     * Get all tags
     * GET /api/tags
     */
    @GetMapping
    public ResponseEntity<List<TagDTO>> getAllTags() {
        log.info("REST request to get all tags");
        List<TagDTO> tags = tagService.getAllTags();
        return ResponseEntity.ok(tags);
    }

    /**
     * Get tag by ID
     * GET /api/tags/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<TagDTO> getTagById(@PathVariable Long id) {
        log.info("REST request to get tag with id: {}", id);
        TagDTO tag = tagService.getTagById(id);
        return ResponseEntity.ok(tag);
    }

    /**
     * Update tag
     * PUT /api/tags/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<TagDTO> updateTag(
            @PathVariable Long id,
            @Valid @RequestBody TagDTO tagDTO) {
        log.info("REST request to update tag with id: {}", id);
        TagDTO updatedTag = tagService.updateTag(id, tagDTO);
        return ResponseEntity.ok(updatedTag);
    }

    /**
     * Delete tag
     * DELETE /api/tags/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTag(@PathVariable Long id) {
        log.info("REST request to delete tag with id: {}", id);
        tagService.deleteTag(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Find tag by name
     * GET /api/tags/by-name?name=xxx
     */
    @GetMapping("/by-name")
    public ResponseEntity<TagDTO> findByName(@RequestParam String name) {
        log.info("REST request to find tag by name: {}", name);
        TagDTO tag = tagService.findTagByName(name);
        return ResponseEntity.ok(tag);
    }
}
