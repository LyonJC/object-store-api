package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.Before;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;

public class ManagedAttributeCRUDIT extends BaseEntityCRUDIT {
  
  private JsonNode jsonNode;  
   
  private ManagedAttribute managedAttributeUnderTest = ManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(new String[] {"a", "b"})
      .description(jsonNode)
      .build();
  
  @Before
  private void setupTest() throws JsonProcessingException{
    
    String json = "{ \"en\" : \"attrEn\", \"fr\" : \"attrFr\"} ";  
    ObjectMapper objectMapper = new ObjectMapper();
    jsonNode = objectMapper.readTree(json) ;      
  }  
      
  @Override
  public void testSave() {
    assertNull(managedAttributeUnderTest.getId());
    save(managedAttributeUnderTest);
    assertNotNull(managedAttributeUnderTest.getId());
  }

  @Override
  public void testFind() {
    ManagedAttribute fetchedObjectStoreMeta = find(ManagedAttribute.class,
        managedAttributeUnderTest.getId());
    assertEquals(managedAttributeUnderTest.getId(), fetchedObjectStoreMeta.getId());
    
    assertArrayEquals(new String[] {"a", "b"}, managedAttributeUnderTest.getAcceptedValues());
    
    assertEquals(jsonNode, managedAttributeUnderTest.getDescription());
    
    assertNotNull(fetchedObjectStoreMeta.getCreatedDate());
  }

  @Override
  public void testRemove() {
    Integer id = managedAttributeUnderTest.getId();
    remove(ManagedAttribute.class, id);
    assertNull(find(ManagedAttribute.class, id));
  }
}
