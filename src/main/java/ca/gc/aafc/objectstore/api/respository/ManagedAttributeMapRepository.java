package ca.gc.aafc.objectstore.api.respository;

import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.UUID;

import javax.inject.Inject;
import javax.transaction.Transactional;
import javax.validation.ValidationException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.objectstore.api.dao.BaseDAO;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto.ManagedAttributeMapValue;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import io.crnk.core.exception.MethodNotAllowedException;
import io.crnk.core.queryspec.QuerySpec;
import io.crnk.core.repository.ResourceRepositoryBase;
import io.crnk.core.resource.list.ResourceList;

/**
 * Crnk requires a repository for each resource type. However,
 * ManagedAttributeMap is a derived/generated object that
 */
@Repository
@Transactional
public class ManagedAttributeMapRepository extends ResourceRepositoryBase<ManagedAttributeMapDto, UUID> {

  private final BaseDAO dao;

  @Inject
  public ManagedAttributeMapRepository(final BaseDAO baseDao) {
    super(ManagedAttributeMapDto.class);
    this.dao = baseDao;
  }

  @Override
  public ResourceList<ManagedAttributeMapDto> findAll(final QuerySpec querySpec) {
    throw new MethodNotAllowedException("method not allowed");
  }

  @Override
  public <S extends ManagedAttributeMapDto> S create(final S resource) {
    // Get the target metadata:
    final UUID metadataUuid = Optional.ofNullable(resource.getMetadata())
      .map(m -> m.getUuid())
      .orElseThrow(() -> new ValidationException("Metadata relationship required to add managed attributes map."));
    final ObjectStoreMetadata metadata = dao.findOneByNaturalId(metadataUuid, ObjectStoreMetadata.class);

    final List<MetadataManagedAttribute> existingAttributes = metadata.getManagedAttribute();
    
    // Loop through the changed attribute values:
    for (final Entry<String, ManagedAttributeMapValue> entry : resource.getValues().entrySet()) {
      final UUID changedAttributeUuid = UUID.fromString(entry.getKey());
      final ManagedAttribute changedAttribute = dao.findOneByNaturalId(changedAttributeUuid, ManagedAttribute.class);
      final String newValue = entry.getValue().getValue();

      final Optional<MetadataManagedAttribute> existingAttributeValue = existingAttributes.stream()
          .filter(existingAttr -> existingAttr.getManagedAttribute() == changedAttribute).findFirst();

      // If this attribute is already set then update the value :
      existingAttributeValue.ifPresent(val -> {
        if (StringUtils.isBlank(newValue)) {
          dao.delete(val);
        } else {
          val.setAssignedValue(newValue);
        }
      });

      // If there is no existing value then create a new one:
      if (!existingAttributeValue.isPresent() && !StringUtils.isBlank(newValue)) {
        final MetadataManagedAttribute newAttributeValue = MetadataManagedAttribute.builder()
          .managedAttribute(changedAttribute)
          .objectStoreMetadata(metadata)
          .assignedValue(newValue)
          .uuid(UUID.randomUUID())
          .build();
        dao.save(newAttributeValue);
      }
    }

    return resource;
  }

}
