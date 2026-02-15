package com.vbforge.security.restjwt.service;

import com.vbforge.security.restjwt.dto.TagDTO;
import com.vbforge.security.restjwt.entity.Tag;
import com.vbforge.security.restjwt.exception.DuplicateResourceException;
import com.vbforge.security.restjwt.exception.ResourceNotFoundException;
import com.vbforge.security.restjwt.mapper.TagMapper;
import com.vbforge.security.restjwt.repository.TagRepository;
import com.vbforge.security.restjwt.service.impl.TagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Service tests for TagService using Mockito.
 */
@ExtendWith(MockitoExtension.class)
class TagServiceTest {

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    private Tag tag;
    private TagDTO tagDTO;

    @BeforeEach
    void setUp() {
        tag = Tag.builder()
                .id(1L)
                .name("Electronics")
                .build();

        tagDTO = TagDTO.builder()
                .id(1L)
                .name("Electronics")
                .build();
    }

    @Test
    void createTag_ShouldReturnCreatedTag() {
        when(tagRepository.existsByName(tagDTO.getName())).thenReturn(false);
        when(tagMapper.toEntity(tagDTO)).thenReturn(tag);
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toDTO(tag)).thenReturn(tagDTO);

        TagDTO result = tagService.createTag(tagDTO);

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electronics");
        verify(tagRepository).save(tag);
    }

    @Test
    void createTag_WhenNameExists_ShouldThrowDuplicateResourceException() {
        when(tagRepository.existsByName(tagDTO.getName())).thenReturn(true);

        assertThatThrownBy(() -> tagService.createTag(tagDTO))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessageContaining("Tag already exists with name");

        verify(tagRepository, never()).save(any());
    }

    @Test
    void getTagById_ShouldReturnTag() {
        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagMapper.toDTO(tag)).thenReturn(tagDTO);

        TagDTO result = tagService.getTagById(1L);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        verify(tagRepository).findById(1L);
    }

    @Test
    void getTagById_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(tagRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.getTagById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tag not found with id: 999");
    }

    @Test
    void getAllTags_ShouldReturnTagList() {
        List<Tag> tags = Arrays.asList(tag);
        List<TagDTO> tagDTOs = Arrays.asList(tagDTO);

        when(tagRepository.findAll()).thenReturn(tags);
        when(tagMapper.toDTOList(tags)).thenReturn(tagDTOs);

        List<TagDTO> result = tagService.getAllTags();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Electronics");
    }

    @Test
    void updateTag_ShouldReturnUpdatedTag() {
        TagDTO updateDTO = TagDTO.builder()
                .name("Electronics")
                .build();

        when(tagRepository.findById(1L)).thenReturn(Optional.of(tag));
        when(tagRepository.save(tag)).thenReturn(tag);
        when(tagMapper.toDTO(tag)).thenReturn(tagDTO);

        TagDTO result = tagService.updateTag(1L, updateDTO);

        assertThat(result).isNotNull();
        verify(tagMapper).updateEntityFromDTO(updateDTO, tag);
        verify(tagRepository).save(tag);
    }

    @Test
    void deleteTag_ShouldDeleteTag() {
        when(tagRepository.existsById(1L)).thenReturn(true);

        tagService.deleteTag(1L);

        verify(tagRepository).deleteById(1L);
    }

    @Test
    void deleteTag_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(tagRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> tagService.deleteTag(999L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(tagRepository, never()).deleteById(any());
    }

    @Test
    void findTagByName_ShouldReturnTag() {
        when(tagRepository.findByName("Electronics")).thenReturn(Optional.of(tag));
        when(tagMapper.toDTO(tag)).thenReturn(tagDTO);

        TagDTO result = tagService.findTagByName("Electronics");

        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Electronics");
    }

    @Test
    void findTagByName_WhenNotFound_ShouldThrowResourceNotFoundException() {
        when(tagRepository.findByName("NonExistent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.findTagByName("NonExistent"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Tag not found with name");
    }
}