package ca.gc.aafc.objectstore.api;

import java.util.Collections;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import ca.gc.aafc.dina.security.DinaAuthenticatedUser;
import ca.gc.aafc.dina.security.DinaRole;

@Configuration
public class DinaAuthenticatedUserConfig {

  public static final String USER_NAME = "test_user";
  public static final Map<String, Set<DinaRole>> ROLES_PER_GROUPS = 
    ImmutableMap.of(
      TestConfiguration.TEST_BUCKET, Collections.singleton(DinaRole.STAFF),
      "Group 2", Collections.singleton(DinaRole.STAFF)
    );

  @Bean
  public DinaAuthenticatedUser dinaAuthenticatedUser() {
    return DinaAuthenticatedUser.builder()
      .username(USER_NAME)
      .rolesPerGroup(ROLES_PER_GROUPS)
      .build();
  }
}
