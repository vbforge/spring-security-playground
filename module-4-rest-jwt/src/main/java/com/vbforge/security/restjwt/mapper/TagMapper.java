package com.vbforge.security.restjwt.mapper;

import com.vbforge.security.restjwt.dto.TagDTO;
import com.vbforge.security.restjwt.entity.Tag;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Tag entity and TagDTO conversions.
 */
@Mapper(componentModel = "spring")
public interface TagMapper {

    /**
     * Convert Tag entity to TagDTO
     */
    @Mapping(target = "name", source = "name")
    TagDTO toDTO(Tag tag);

    /**
     * Convert TagDTO to Tag entity
     */
    Tag toEntity(TagDTO tagDTO);

    /**
     * Convert list of Tag entities to list of TagDTOs
     */
    List<TagDTO> toDTOList(List<Tag> tags);

    /**
     * Update existing Tag entity from TagDTO
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "products", ignore = true)
    void updateEntityFromDTO(TagDTO tagDTO, @MappingTarget Tag tag);
}