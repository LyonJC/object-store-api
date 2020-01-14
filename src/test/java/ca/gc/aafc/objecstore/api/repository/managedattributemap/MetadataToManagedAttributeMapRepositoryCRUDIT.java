package ca.gc.aafc.objecstore.api.repository.managedattributemap;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import ca.gc.aafc.objecstore.api.repository.BaseRepositoryTest;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeMapDto.ManagedAttributeMapValue;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.MetadataManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import ca.gc.aafc.objectstore.api.respository.managedattributemap.MetadataToManagedAttributeMapRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.MetadataManagedAttributeFactory;
import ca.gc.aafc.objectstore.api.testsupport.factories.ObjectStoreMetadataFactory;
import io.crnk.core.queryspec.QuerySpec;

public class MetadataToManagedAttributeMapRepositoryCRUDIT extends BaseRepositoryTest {
  
  @Inject
  private MetadataToManagedAttributeMapRepository metadataToManagedAttributeMapRepository;

  @Inject
  private EntityManager entityManager;

  private ObjectStoreMetadata testMetadata;
  private ManagedAttribute testManagedAttribute1;
  private ManagedAttribute testManagedAttribute2;
  private MetadataManagedAttribute testAttr1Value;
  private MetadataManagedAttribute testAttr2Value;

  @BeforeEach
  public void setup() {
    testMetadata = ObjectStoreMetadataFactory.newObjectStoreMetadata().build();
    entityManager.persist(testMetadata);

    testManagedAttribute1 = ManagedAttributeFactory.newManagedAttribute().name("attr1").build();
    entityManager.persist(testManagedAttribute1);

    testManagedAttribute2 = ManagedAttributeFactory.newManagedAttribute().name("attr2").build();
    entityManager.persist(testManagedAttribute2);

    testAttr1Value = MetadataManagedAttributeFactory.newMetadataManagedAttribute()
      .assignedValue("test attr1 value")
      .managedAttribute(testManagedAttribute1)
      .objectStoreMetadata(testMetadata)
      .build();
    entityManager.persist(testAttr1Value);

    testAttr2Value = MetadataManagedAttributeFactory.newMetadataManagedAttribute()
      .assignedValue("test attr2 value")
      .managedAttribute(testManagedAttribute2)
      .objectStoreMetadata(testMetadata)
      .build();
    entityManager.persist(testAttr2Value);

    entityManager.flush();
    entityManager.refresh(testMetadata);
  }

  @Test
  public void findAttributeMapsByMetadata_when2ValuesExist_returnMapWith2Values() {
    Map<UUID, ManagedAttributeMapDto> resultMap = metadataToManagedAttributeMapRepository.findOneRelations(
      Collections.singletonList(testMetadata.getUuid()),
      "managedAttributeMap",
      new QuerySpec(ManagedAttributeMapDto.class)
    );

    ManagedAttributeMapDto attributeMap = resultMap.get(testMetadata.getUuid());

    ManagedAttributeMapValue attr1Value = attributeMap.getValues()
      .get(testManagedAttribute1.getUuid().toString());
    ManagedAttributeMapValue attr2Value = attributeMap.getValues()
      .get(testManagedAttribute2.getUuid().toString());

    // The ManagedAttributeMap should have an ID based on its Metadata:
    assertEquals("metadata/" + testMetadata.getUuid() + "/managedAttributeMap", attributeMap.getId());

    assertEquals("attr1", attr1Value.getName());
    assertEquals("test attr1 value", attr1Value.getValue());

    assertEquals("attr2", attr2Value.getName());
    assertEquals("test attr2 value", attr2Value.getValue());
  }

}
