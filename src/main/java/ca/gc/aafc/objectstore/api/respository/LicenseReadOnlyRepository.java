package ca.gc.aafc.objectstore.api.respository;

import ca.gc.aafc.objectstore.api.YamlPropertyLoaderFactory;
import org.springframework.context.annotation.PropertySource;

@PropertySource(value = "classpath:licences.yml", factory = YamlPropertyLoaderFactory.class)
public class LicenseReadOnlyRepository {
}
