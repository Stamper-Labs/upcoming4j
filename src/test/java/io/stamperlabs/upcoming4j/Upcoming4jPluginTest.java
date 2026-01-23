package io.stamperlabs.upcoming4j;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import io.stamperlabs.upcoming4j.process.GitProcessBuilderFactory;
import io.stamperlabs.upcoming4j.service.CommitsSinceTag;
import io.stamperlabs.upcoming4j.service.LatestGitTagService;
import io.stamperlabs.upcoming4j.service.NextVersion;
import java.io.File;
import java.util.List;
import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

class Upcoming4jPluginTest {

  @TempDir File tempDir;

  @Test
  void apply_setsNextVersion_whenGitProject() {
    // given
    new File(tempDir, ".git").mkdir();

    Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();

    ProcessBuilder dummyPb = mock(ProcessBuilder.class);

    try (MockedStatic<GitProcessBuilderFactory> factoryMock =
            mockStatic(GitProcessBuilderFactory.class);
        MockedConstruction<LatestGitTagService> latestTagMock =
            mockConstruction(
                LatestGitTagService.class,
                (mock, context) -> when(mock.retrieve()).thenReturn("v1.2.3"));
        MockedConstruction<CommitsSinceTag> commitsMock =
            mockConstruction(
                CommitsSinceTag.class,
                (mock, context) ->
                    when(mock.retrieve()).thenReturn(List.of("feat: add new feature")));
        MockedConstruction<NextVersion> nextVersionMock =
            mockConstruction(
                NextVersion.class,
                (mock, context) ->
                    when(mock.compute("v1.2.3", List.of("feat: add new feature")))
                        .thenReturn("1.3.0"))) {

      factoryMock.when(() -> GitProcessBuilderFactory.fetchTags(any())).thenReturn(dummyPb);
      factoryMock.when(() -> GitProcessBuilderFactory.latestTag(any())).thenReturn(dummyPb);
      factoryMock
          .when(() -> GitProcessBuilderFactory.logSinceTag(any(), anyString()))
          .thenReturn(dummyPb);

      Upcoming4jPlugin plugin = new Upcoming4jPlugin();

      // when
      plugin.apply(project);

      // then
      Object nextVersion = project.getExtensions().getExtraProperties().get("nextVersion");

      assertEquals("1.3.0", nextVersion);
    }
  }

  @Test
  void apply_throwsException_whenProjectIsNotGitRepository() {
    Project project = ProjectBuilder.builder().withProjectDir(tempDir).build();

    Upcoming4jPlugin plugin = new Upcoming4jPlugin();

    assertThrows(GradleException.class, () -> plugin.apply(project));
  }
}
