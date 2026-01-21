package io.stamperlabs.upcoming4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LatestGitTagTest {

  @Mock private Project project;

  @Mock private Logger logger;

  @Mock private ProcessBuilder gitFetchProcessBuilder;

  @Mock private ProcessBuilder gitForEachRefProcessBuilder;

  @Mock private Process fetchProcess;

  @Mock private Process forEachRefProcess;

  @BeforeEach
  void setUp() {
    when(project.getLogger()).thenReturn(logger);
  }

  @Test
  void retrieveReturnsLatestTagWhenPresent() throws Exception {
    when(gitFetchProcessBuilder.command())
        .thenReturn(java.util.List.of("git", "fetch", "--tags", "--prune"));
    when(gitForEachRefProcessBuilder.command())
        .thenReturn(java.util.List.of("git", "for-each-ref"));

    when(gitFetchProcessBuilder.start()).thenReturn(fetchProcess);
    when(fetchProcess.waitFor()).thenReturn(0);

    when(gitForEachRefProcessBuilder.start()).thenReturn(forEachRefProcess);
    when(forEachRefProcess.getInputStream())
        .thenReturn(new ByteArrayInputStream("v1.2.3\n".getBytes()));
    when(forEachRefProcess.waitFor()).thenReturn(0);

    LatestGitTag latestGitTag =
        new LatestGitTag(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    String result = latestGitTag.retrieve();

    assertEquals("v1.2.3", result);
  }

  @Test
  void retrieveReturnsEmptyStringWhenNoTagsExist() throws Exception {
    when(gitFetchProcessBuilder.command())
        .thenReturn(java.util.List.of("git", "fetch", "--tags", "--prune"));
    when(gitForEachRefProcessBuilder.command())
        .thenReturn(java.util.List.of("git", "for-each-ref"));

    when(gitFetchProcessBuilder.start()).thenReturn(fetchProcess);
    when(fetchProcess.waitFor()).thenReturn(0);

    when(gitForEachRefProcessBuilder.start()).thenReturn(forEachRefProcess);
    when(forEachRefProcess.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(forEachRefProcess.waitFor()).thenReturn(0);

    LatestGitTag latestGitTag =
        new LatestGitTag(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    String result = latestGitTag.retrieve();

    assertEquals("", result);
  }

  @Test
  void retrieveThrowsWhenGitFetchFails() throws Exception {
    when(gitFetchProcessBuilder.command())
        .thenReturn(java.util.List.of("git", "fetch", "--tags", "--prune"));
    when(gitFetchProcessBuilder.start()).thenReturn(fetchProcess);
    when(fetchProcess.waitFor()).thenReturn(1);

    LatestGitTag latestGitTag =
        new LatestGitTag(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    RuntimeException ex = assertThrows(RuntimeException.class, latestGitTag::retrieve);

    assertEquals("Git fetch command failed with exit code 1", ex.getMessage());
  }

  @Test
  void retrieveThrowsWhenForEachRefFails() throws Exception {
    when(gitFetchProcessBuilder.command())
        .thenReturn(java.util.List.of("git", "fetch", "--tags", "--prune"));
    when(gitForEachRefProcessBuilder.command())
        .thenReturn(java.util.List.of("git", "for-each-ref"));

    when(gitFetchProcessBuilder.start()).thenReturn(fetchProcess);
    when(fetchProcess.waitFor()).thenReturn(0);

    when(gitForEachRefProcessBuilder.start()).thenReturn(forEachRefProcess);
    when(forEachRefProcess.getInputStream())
        .thenReturn(new ByteArrayInputStream("v1.0.0\n".getBytes()));
    when(forEachRefProcess.waitFor()).thenReturn(2);

    LatestGitTag latestGitTag =
        new LatestGitTag(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    RuntimeException ex = assertThrows(RuntimeException.class, latestGitTag::retrieve);

    assertEquals("Git for-each-ref command failed with exit code 2", ex.getMessage());
  }
}
