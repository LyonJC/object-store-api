package ca.gc.aafc.objectstore.api;

import org.springframework.boot.SpringApplication;

/**
 * Launches the application.
 */
//CHECKSTYLE:OFF HideUtilityClassConstructor (Configuration class can not have invisible constructor, ignore the check style error for this case)
public class ObjectStoreApiLauncher {
  public static void main(String[] args) {
    SpringApplication.run(ObjectStoreApiLauncher.class, args);
  }
}
