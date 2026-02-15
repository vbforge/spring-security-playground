package com.vbforge.security.websec.service;

import com.vbforge.security.websec.dto.TagDTO;

import java.util.List;

/**
 * Service interface for Tag operations.
 */
public interface TagService {

    /**
     * Create a new tag
     */
    TagDTO createTag(TagDTO tagDTO);

    /**
     * Get tag by ID
     */
    TagDTO getTagById(Long id);

    /**
     * Get all tags
     */
    List<TagDTO> getAllTags();

    /**
     * Update existing tag
     */
    TagDTO updateTag(Long id, TagDTO tagDTO);

    /**
     * Delete tag by ID
     */
    void deleteTag(Long id);

    /**
     * Find tag by name
     */
    TagDTO findTagByName(String name);
}
