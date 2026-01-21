package io.stamperlabs.upcoming4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NextVersionTest {

  private Project project;
  private Logger logger;
  private NextVersion nextVersion;

  @BeforeEach
  void setUp() {
    project = mock(Project.class);
    logger = mock(Logger.class);
    when(project.getLogger()).thenReturn(logger);

    nextVersion = new NextVersion(project);
  }

  @Test
  void returnsSameVersionWhenNoCommits() {
    String result = nextVersion.compute("1.2.3", List.of());

    assertEquals("1.2.3", result);
  }

  @Test
  void bumpsPatchVersionOnFixCommit() {
    List<String> commits = List.of("fix: correct typo");

    String result = nextVersion.compute("1.2.3", commits);

    assertEquals("1.2.4", result);
  }

  @Test
  void bumpsMinorVersionOnFeatCommit() {
    List<String> commits = List.of("feat: add new feature");

    String result = nextVersion.compute("1.2.3", commits);

    assertEquals("1.3.0", result);
  }

  @Test
  void bumpsMajorVersionOnBreakingChangeText() {
    List<String> commits = List.of("feat: refactor api", "BREAKING CHANGE: api changed");

    String result = nextVersion.compute("1.2.3", commits);

    assertEquals("2.0.0", result);
  }

  @Test
  void bumpsMajorVersionOnExclamationMarkConvention() {
    List<String> commits = List.of("feat!: change api behavior");

    String result = nextVersion.compute("1.2.3", commits);

    assertEquals("2.0.0", result);
  }

  @Test
  void normalizesTagStartingWithV() {
    List<String> commits = List.of("fix: bug fix");

    String result = nextVersion.compute("v1.2.3", commits);

    assertEquals("1.2.4", result);
  }

  @Test
  void throwsExceptionForInvalidSemanticVersion() {
    List<String> commits = List.of("feat: new feature");

    IllegalArgumentException ex =
        assertThrows(IllegalArgumentException.class, () -> nextVersion.compute("invalid", commits));

    assertEquals("Current tag 'invalid' is not semantic version format (X.Y.Z)", ex.getMessage());
  }
}
