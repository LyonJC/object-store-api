package ca.gc.aafc.objectstore.api.repository;

import ca.gc.aafc.objectstore.api.respository.LicenseReadOnlyRepository;
import lombok.extern.log4j.Log4j2;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.inject.Inject;

@Log4j2
public class LicenseReadOnlyRepositoryTest {

    @Autowired
    private LicenseReadOnlyRepository readOnlyRepository;

    @Test
    public void ObtainClassValues() {
        log.info("THERE ARE: " + readOnlyRepository.getLicenses().size() + " LICENSES");

    }
}
