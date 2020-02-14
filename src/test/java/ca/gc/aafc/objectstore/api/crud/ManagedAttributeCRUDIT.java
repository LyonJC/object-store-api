package ca.gc.aafc.objectstore.api.crud;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.util.HashMap;
import java.util.Map;

import org.junit.Before;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.testsupport.factories.ManagedAttributeFactory;

public class ManagedAttributeCRUDIT extends BaseEntityCRUDIT {
  
  private Map<String, String> testDesc;  
   
  private ManagedAttribute managedAttributeUnderTest = ManagedAttributeFactory.newManagedAttribute()
      .acceptedValues(new String[] {"a", "b"})
      .description(testDesc)
      .build();
  
  @Before
  private void setupTest() {    
    testDesc = new HashMap<String,String>();
    testDesc.put("en","attrEn");
    testDesc.put("fr","attrFr");       
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
    
    assertEquals(testDesc, managedAttributeUnderTest.getDescription());
    
    assertNotNull(fetchedObjectStoreMeta.getCreatedDate());
  }

  @Override
  public void testRemove() {
    Integer id = managedAttributeUnderTest.getId();
    remove(ManagedAttribute.class, id);
    assertNull(find(ManagedAttribute.class, id));
  }
}
