package ca.gc.aafc.objectstore.api.mapper;

import java.util.List;

import org.mapstruct.Context;
import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import ca.gc.aafc.objectstore.api.dto.MetadataManagedAttributeDto;
import ca.gc.aafc.objectstore.api.dto.ObjectStoreMetadataDto;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;

@Mapper(componentModel = "spring")
public interface MetadataManagedAttributeMapper {
  
  MetadataManagedAttributeMapper INSTANCE = Mappers.getMapper(MetadataManagedAttributeMapper.class);

  /**
   * objectStoreMetadata.managedAttribute property will be ignore to avoid circular dependency
   * 
   * @param dto
   * @return
   */
  @Mapping(target = "objectStoreMetadata", qualifiedByName = "objectStoreMetadataIdOnly")
  MetadataManagedAttributeDto toDto(MetadataManagedAttribute entity, @Context CycleAvoidingMappingContext context);
  
  List<MetadataManagedAttributeDto> toDtoList(List<MetadataManagedAttribute> entities, @Context CycleAvoidingMappingContext context);

  @Mapping(target = "objectStoreMetadata", ignore = true)
  @Mapping(target = "managedAttribute", ignore = true)
  MetadataManagedAttribute toEntity(MetadataManagedAttributeDto dto);

  @InheritConfiguration
  void updateMetadataManagedAttributeFromDto(MetadataManagedAttributeDto dto,
      @MappingTarget MetadataManagedAttribute entity);

  /**
   * Used to avoid cyclic reference since objectStoreMetadata points back to
   * MetadataManagedAttribute.
   * 
   * @param osm
   * @return
   */
  @Named("objectStoreMetadataIdOnly")
  default ObjectStoreMetadataDto objectStoreMetadataToObjectStoreMetadataDto(ObjectStoreMetadata osm, @Context CycleAvoidingMappingContext context) {
    if (osm == null) {
      return null;
    }
    
    // Get a builder from the current instance and set the relationships to null
    ObjectStoreMetadata osm2 = osm.toBuilder()
        .managedAttribute(null)
        .build();
    return ObjectStoreMetadataMapper.INSTANCE.toDto(osm2, null, context);
  }
  
}
