package com.vbforge.security.defaultsec.mapper;

import com.vbforge.security.defaultsec.dto.ProductDTO;
import com.vbforge.security.defaultsec.entity.Product;
import org.mapstruct.*;

import java.util.List;

/**
 * MapStruct mapper for Product entity and ProductDTO conversions.
 * 
 * MapStruct will generate the implementation at compile time.
 * The @Mapper annotation with componentModel = "spring" makes it a Spring bean.
 */
@Mapper(componentModel = "spring", uses = {TagMapper.class})
public interface ProductMapper {

    /**
     * Convert Product entity to ProductDTO
     */
    ProductDTO toDTO(Product product);

    /**
     * Convert ProductDTO to Product entity
     */
    @Mapping(target = "createdAt", ignore = true)  // Managed by @CreationTimestamp
    Product toEntity(ProductDTO productDTO);

    /**
     * Convert list of Product entities to list of ProductDTOs
     */
    List<ProductDTO> toDTOList(List<Product> products);

    /**
     * Update existing Product entity from ProductDTO
     * Useful for PATCH/PUT operations
     */
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    void updateEntityFromDTO(ProductDTO productDTO, @MappingTarget Product product);
}