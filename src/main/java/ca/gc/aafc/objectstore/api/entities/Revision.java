package ca.gc.aafc.objectstore.api.entities;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.hibernate.envers.EntityTrackingRevisionListener;
import org.hibernate.envers.RevisionEntity;
import org.hibernate.envers.RevisionNumber;
import org.hibernate.envers.RevisionTimestamp;
import org.hibernate.envers.RevisionType;

import ca.gc.aafc.objectstore.api.entities.Revision.ObjectStoreRevisionListener;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Entity
@Data
@RevisionEntity(ObjectStoreRevisionListener.class)
public class Revision {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @RevisionNumber
  private Integer id;

  private String username;

  @RevisionTimestamp
  private long timestamp;

  @Named
  @RequiredArgsConstructor(onConstructor=@__({ @Inject }))
  public static class ObjectStoreRevisionListener implements EntityTrackingRevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
      Revision revision = (Revision) revisionEntity;
    }

    @Override
    public void entityChanged(
      Class entityClass,
      String entityName,
      Serializable entityId,
      RevisionType revisionType,
      Object revisionEntity
    ) {
      Revision revision = (Revision) revisionEntity;
    }

  }

}
