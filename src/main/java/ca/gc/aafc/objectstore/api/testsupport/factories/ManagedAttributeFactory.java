package ca.gc.aafc.objectstore.api.testsupport.factories;

import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;

import ca.gc.aafc.objectstore.api.entities.ManagedAttribute;
import ca.gc.aafc.objectstore.api.entities.ManagedAttribute.ManagedAttributeType;
import ca.gc.aafc.dina.testsupport.factories.TestableEntityFactory;

public class ManagedAttributeFactory implements TestableEntityFactory<ManagedAttribute> {

  @Override
  public ManagedAttribute getEntityInstance() {
    return newManagedAttribute().build();
  }
  
  /**
   * Static method that can be called to return a configured builder that can be further customized
   * to return the actual entity object, call the .build() method on a builder.
   * 
   * @return Pre-configured builder with all mandatory fields set
   */
  public static ManagedAttribute.ManagedAttributeBuilder newManagedAttribute() {
    return ManagedAttribute.builder()
        .uuid(UUID.randomUUID())
        .name(TestableEntityFactory.generateRandomNameLettersOnly(12))
        .managedAttributeType(ManagedAttributeType.STRING);
   } 
  
  /**
   * A utility method to create a list of qty number of Chains with no configuration.
   * 
   * @param qty The number of Chains populated in the list
   * @return List of Chain
   */
  public static List<ManagedAttribute> newListOf(int qty) {
    return newListOf(qty, null);
  }

  /**
   * A utility method to create a list of qty number of Chain with an incrementing attribute
   * based on the configuration argument. An example of configuration would be the functional
   * interface (bldr, index) -> bldr.name(" string" + index)
   * 
   * @param qty           The number of Chain that is populated in the list.
   * @param configuration the function to apply, usually to differentiate the different entities in
   *                      the list.
   * @return List of Chain
   */
  public static List<ManagedAttribute> newListOf(int qty,
      BiFunction<ManagedAttribute.ManagedAttributeBuilder, Integer, ManagedAttribute.ManagedAttributeBuilder> configuration) {
    
    return TestableEntityFactory.newEntity(qty, ManagedAttributeFactory::newManagedAttribute, configuration,
        ManagedAttribute.ManagedAttributeBuilder::build);
  }

}
