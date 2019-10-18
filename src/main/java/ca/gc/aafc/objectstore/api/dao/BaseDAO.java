package ca.gc.aafc.objectstore.api.dao;

import java.util.UUID;
import java.util.function.Consumer;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.hibernate.Session;
import org.hibernate.SimpleNaturalIdLoadAccess;
import org.springframework.stereotype.Component;

/**
 * Base Data Access Object layer. This class should be the only one holding a reference to the {@link EntityManager}.
 *
 */
@Component
public class BaseDAO {
  
  @PersistenceContext
  private EntityManager entityManager;

  /**
   * Find an entity by it's naturalId. The method assumes that the naturalId is unique.
   * 
   * @param uuid
   * @param entityClass
   * @return
   */
  public <T> T findOneByNaturalId(UUID uuid, Class<T> entityClass) {
    T objectStoreMetadata = entityManager.unwrap(Session.class)
        .bySimpleNaturalId(entityClass)
        .load(uuid);
    return objectStoreMetadata;
  }
  
  /**
   * Give a reference to an entity that should exist without actually loading it. Useful to set
   * relationships without loading the entity.
   * 
   * @param entityClass
   * @param uuid
   * @return
   */
  public <T> T getReferenceByNaturalId(Class<T> entityClass, UUID uuid) {
    SimpleNaturalIdLoadAccess<T> loadAccess = entityManager.unwrap(Session.class)
        .bySimpleNaturalId(entityClass);
    return loadAccess.getReference(uuid);
  }
  
  /**
   * Set a relationship by calling the provided {@link Consumer} with a reference Entity loaded by
   * NaturalId.
   * 
   * @param entityClass
   * @param uuid
   * @param objConsumer
   */
  public <T> void setRelationshipUsing(Class<T> entityClass, UUID uuid, Consumer<T> objConsumer) {
    objConsumer.accept(getReferenceByNaturalId(entityClass, uuid));
  }
  
  /**
   * Save the provided entity.
   * 
   * @param entity
   */
  public void save(Object entity) {
    entityManager.persist(entity);
  }
  
  /**
   * Delete the provided entity.
   * 
   * @param entity
   */
  public void delete(Object entity) {
    entityManager.remove(entity);
  }
  
}
