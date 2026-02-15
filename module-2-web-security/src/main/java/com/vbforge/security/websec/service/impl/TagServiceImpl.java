package com.vbforge.security.websec.service.impl;

import com.vbforge.security.websec.dto.TagDTO;
import com.vbforge.security.websec.entity.Tag;
import com.vbforge.security.websec.exception.DuplicateResourceException;
import com.vbforge.security.websec.exception.ResourceNotFoundException;
import com.vbforge.security.websec.mapper.TagMapper;
import com.vbforge.security.websec.repository.TagRepository;
import com.vbforge.security.websec.service.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Implementation of TagService.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Override
    @Transactional
    public TagDTO createTag(TagDTO tagDTO) {
        log.info("Creating new tag: {}", tagDTO.getName());

        // Check if tag with same name already exists
        if (tagRepository.existsByName(tagDTO.getName())) {
            throw new DuplicateResourceException("Tag", "name", tagDTO.getName());
        }

        Tag tag = tagMapper.toEntity(tagDTO);
        Tag savedTag = tagRepository.save(tag);

        log.info("Tag created successfully with id: {}", savedTag.getId());
        return tagMapper.toDTO(savedTag);
    }

    @Override
    public TagDTO getTagById(Long id) {
        log.info("Fetching tag with id: {}", id);

        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        return tagMapper.toDTO(tag);
    }

    @Override
    public List<TagDTO> getAllTags() {
        log.info("Fetching all tags");

        List<Tag> tags = tagRepository.findAll();
        return tagMapper.toDTOList(tags);
    }

    @Override
    @Transactional
    public TagDTO updateTag(Long id, TagDTO tagDTO) {
        log.info("Updating tag with id: {}", id);

        Tag existingTag = tagRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", id));

        // Check if updating name to an existing name
        if (tagDTO.getName() != null &&
                !existingTag.getName().equals(tagDTO.getName()) &&
                tagRepository.existsByName(tagDTO.getName())) {
            throw new DuplicateResourceException("Tag", "name", tagDTO.getName());
        }

        tagMapper.updateEntityFromDTO(tagDTO, existingTag);
        Tag updatedTag = tagRepository.save(existingTag);

        log.info("Tag updated successfully with id: {}", id);
        return tagMapper.toDTO(updatedTag);
    }

    @Override
    @Transactional
    public void deleteTag(Long id) {
        log.info("Deleting tag with id: {}", id);

        if (!tagRepository.existsById(id)) {
            throw new ResourceNotFoundException("Tag", id);
        }

        tagRepository.deleteById(id);
        log.info("Tag deleted successfully with id: {}", id);
    }

    @Override
    public TagDTO findTagByName(String name) {
        log.info("Finding tag by name: {}", name);

        Tag tag = tagRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("Tag", "name", name));

        return tagMapper.toDTO(tag);
    }
}
