package ca.gc.aafc.objectstore.api.respository;

import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Repository;

import ca.gc.aafc.dina.filter.DinaFilterResolver;
import ca.gc.aafc.dina.mapper.DinaMapper;
import ca.gc.aafc.dina.repository.DinaRepository;
import ca.gc.aafc.dina.service.DinaService;
import ca.gc.aafc.objectstore.api.dto.ObjectSubtypeDto;
import ca.gc.aafc.objectstore.api.entities.ObjectSubtype;
import io.crnk.core.exception.ForbiddenException;
import lombok.NonNull;

@Repository
@Transactional
public class ObjectSubtypeResourceRepository
    extends DinaRepository<ObjectSubtypeDto, ObjectSubtype> {

  private final DinaService<ObjectSubtype> dinaService;
  private final MessageSource messageSource;

  public ObjectSubtypeResourceRepository(
    @NonNull DinaService<ObjectSubtype> dinaService,
    @NonNull DinaFilterResolver filterResolver,
    MessageSource messageSource
  ) {
    super(
      dinaService,
      Optional.ofNullable(null),
      new DinaMapper<>(ObjectSubtypeDto.class),
      ObjectSubtypeDto.class,
      ObjectSubtype.class,
      filterResolver);
    this.dinaService = dinaService;
    this.messageSource = messageSource;
  }

  @Override
  public <S extends ObjectSubtypeDto> S save(S resource) {
    ObjectSubtype entity = dinaService.findOne(resource.getUuid(), ObjectSubtype.class);
    if (entity.isAppManaged()) {
      throw new ForbiddenException(
          messageSource.getMessage("error.appManaged.read_only", null, LocaleContextHolder.getLocale()));
    }
    return super.save(resource);
  }

}
