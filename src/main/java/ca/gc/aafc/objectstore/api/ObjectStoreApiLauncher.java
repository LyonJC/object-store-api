package ca.gc.aafc.objectstore.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

/**
 * Launches the application.
 */
//CHECKSTYLE:OFF HideUtilityClassConstructor (Configuration class can not have invisible constructor, ignore the check style error for this case)
@SpringBootApplication
@EnableConfigurationProperties(ObjectStoreConfiguration.class)
public class ObjectStoreApiLauncher {
  public static void main(String[] args) {
    SpringApplication.run(ObjectStoreApiLauncher.class, args);
  }
}
