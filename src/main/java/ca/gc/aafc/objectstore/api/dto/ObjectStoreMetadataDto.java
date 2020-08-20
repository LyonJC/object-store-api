package ca.gc.aafc.objectstore.api.dto;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import javax.persistence.Id;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.PropertyName;
import org.javers.core.metamodel.annotation.ShallowReference;
import org.javers.core.metamodel.annotation.TypeName;

import ca.gc.aafc.dina.dto.RelatedEntity;
import ca.gc.aafc.objectstore.api.entities.DcType;
import ca.gc.aafc.objectstore.api.entities.ObjectStoreMetadata;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.crnk.core.resource.annotations.JsonApiId;
import io.crnk.core.resource.annotations.JsonApiRelation;
import io.crnk.core.resource.annotations.JsonApiResource;
import io.crnk.core.resource.annotations.LookupIncludeBehavior;
import lombok.Data;

@SuppressFBWarnings({ "EI_EXPOSE_REP", "EI_EXPOSE_REP2" })
@RelatedEntity(ObjectStoreMetadata.class)
@Data
@JsonApiResource(type = ObjectStoreMetadataDto.TYPENAME)
@TypeName(ObjectStoreMetadataDto.TYPENAME)
public class ObjectStoreMetadataDto {

  public static final String TYPENAME = "metadata";

  @JsonApiId
  @Id
  @PropertyName("id")
  private UUID uuid;

  private String bucket;
  private UUID fileIdentifier;
  private String fileExtension;

  private String dcFormat;

  @ShallowReference
  private DcType dcType;

  @JsonInclude(Include.NON_EMPTY)
  private String acCaption;

  private OffsetDateTime acDigitizationDate;

  @DiffIgnore
  private OffsetDateTime xmpMetadataDate;

  private String xmpRightsWebStatement;
  private String dcRights;
  private String xmpRightsOwner;
  private String xmpRightsUsageTerms;
  
  @JsonInclude(Include.NON_EMPTY)
  private String originalFilename;

  private String acHashFunction;
  private String acHashValue;

  @DiffIgnore
  private OffsetDateTime createdDate;
  @JsonInclude(Include.NON_EMPTY)
  private OffsetDateTime deletedDate;
  
  @JsonInclude(Include.NON_EMPTY)
  private String[] acTags;
  
  @JsonApiRelation
  @DiffIgnore
  private List<MetadataManagedAttributeDto> managedAttribute;

  // AUTOMATICALLY_ALWAYS because it should be fetched using a call to
  // MetadataToManagedAttributeMapRepository.
  @JsonApiRelation(lookUp = LookupIncludeBehavior.AUTOMATICALLY_ALWAYS)
  private ManagedAttributeMapDto managedAttributeMap;

  private UUID acMetadataCreator;

  @JsonApiRelation
  @ShallowReference
  private ObjectStoreMetadataDto acDerivedFrom;

  private UUID dcCreator;

  private boolean publiclyReleasable;

  @JsonInclude(Include.NON_EMPTY)
  private String notPubliclyReleasableReason;

  @JsonInclude(Include.NON_EMPTY)
  private String acSubType;
  
  private String group;

}
