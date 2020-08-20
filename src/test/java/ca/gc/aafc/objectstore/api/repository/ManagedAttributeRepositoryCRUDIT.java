package ca.gc.aafc.objectstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.UUID;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.collect.ImmutableMap;

import ca.gc.aafc.objectstore.api.DinaAuthenticatedUserConfig;
import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import ca.gc.aafc.objectstore.api.respository.ManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import io.crnk.core.queryspec.QuerySpec;

public class ManagedAttributeRepositoryCRUDIT extends BaseRepositoryTest {
  
  @Inject
  private ManagedAttributeResourceRepository managedResourceRepository;
  
  private ManagedAttribute testManagedAttribute;

  private final static String DINA_USER_NAME = DinaAuthenticatedUserConfig.USER_NAME;

  private ManagedAttribute createTestManagedAttribute() throws JsonProcessingException {
    testManagedAttribute = ManagedAttributeFactory.newManagedAttribute()
        .acceptedValues(new String[] { "dosal" })
        .description(ImmutableMap.of("en", "attrEn", "fr", "attrFr"))
        .build();

    persist(testManagedAttribute);
    return testManagedAttribute;
  }
  
  @BeforeEach
  public void setup() throws JsonProcessingException { 
    createTestManagedAttribute();    
  }  

  @Test
  public void findManagedAttribute_whenNoFieldsAreSelected_manageAttributeReturnedWithAllFields() {
    ManagedAttributeDto managedAttributeDto = managedResourceRepository
        .findOne(testManagedAttribute.getUuid(), new QuerySpec(ManagedAttributeDto.class));
    assertNotNull(managedAttributeDto);
    assertEquals(testManagedAttribute.getUuid(), managedAttributeDto.getUuid());
    assertArrayEquals(testManagedAttribute.getAcceptedValues(),
        managedAttributeDto.getAcceptedValues());
    assertEquals(testManagedAttribute.getManagedAttributeType(),
        managedAttributeDto.getManagedAttributeType());
    assertEquals(testManagedAttribute.getName(), managedAttributeDto.getName());
    assertEquals(testManagedAttribute.getDescription().get("en"),
        managedAttributeDto.getDescription().get("en"));
  }

  @Test
  public void create_WithAuthenticatedUser_SetsCreatedBy() {
    ManagedAttributeDto ma = new ManagedAttributeDto();
    ma.setUuid(UUID.randomUUID());
    ma.setName("name");
    ma.setManagedAttributeType(ManagedAttributeType.STRING);
    ma.setAcceptedValues(new String[] { "dosal" });
    ma.setDescription(ImmutableMap.of("en", "attrEn", "fr", "attrFr"));

    ManagedAttributeDto result = managedResourceRepository.findOne(
      managedResourceRepository.create(ma).getUuid(),
      new QuerySpec(ManagedAttributeDto.class));
    assertEquals(DINA_USER_NAME, result.getCreatedBy());
  }
    
}
