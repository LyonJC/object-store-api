package ca.gc.aafc.objecstore.api.repository;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import javax.inject.Inject;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.vladmihalcea.hibernate.type.json.JsonNodeBinaryType;

import ca.gc.aafc.objectstore.api.dto.ManagedAttributeDto;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.respository.ManagedAttributeResourceRepository;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;
import io.crnk.core.queryspec.QuerySpec;

public class ManagedAttributeRepositoryCRUDIT extends BaseRepositoryTest {
  
  @Inject
  private ManagedAttributeResourceRepository managedResourceRepository;
  
  private ManagedAttribute testManagedAttribute;
  
  private ManagedAttribute createTestManagedAttribute() throws JsonProcessingException{
    testManagedAttribute = ManagedAttributeFactory.newManagedAttribute().build();
    testManagedAttribute.setAcceptedValues(new String[] {"dosal"});    
    
    String json = "{ \"en\" : \"attrEn\", \"fr\" : \"attrFr\"} ";
    ObjectMapper objectMapper = new ObjectMapper();
    JsonNode jsonNode = objectMapper.readTree(json);    
    testManagedAttribute.setDescription(jsonNode);  
    
    persist(testManagedAttribute);
    return testManagedAttribute;
  }
  
  @BeforeEach
  public void setup() throws JsonProcessingException { 
    createTestManagedAttribute();    
  }  

  @Test
  public void findManagedAttribute_whenNoFieldsAreSelected_manageAttributeReturnedWithAllFields() {
    ManagedAttributeDto managedAttributeDto = managedResourceRepository.findOne(
        testManagedAttribute.getUuid(),
        new QuerySpec(ManagedAttributeDto.class)
    );  
    assertNotNull(managedAttributeDto);
    assertEquals(testManagedAttribute.getUuid(), managedAttributeDto.getUuid());
    System.out.println("managedAttributeDto.getAcceptedValues() " +managedAttributeDto.getAcceptedValues());
    assertArrayEquals(testManagedAttribute.getAcceptedValues(), 
        (String[])(managedAttributeDto.getAcceptedValues().toArray(new String[0])));
    assertEquals(testManagedAttribute.getManagedAttributeType(), 
        managedAttributeDto.getManagedAttributeType());
    assertEquals(testManagedAttribute.getName(), managedAttributeDto.getName());    
    assertEquals(testManagedAttribute.getDescription(), managedAttributeDto.getDescription());
    
  }
    
}
