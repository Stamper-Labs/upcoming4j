package io.stamperlabs.upcoming4j.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.stamperlabs.upcoming4j.exception.Upcoming4jException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CommitsSinceTagTest {

  @Mock private ProcessBuilder processBuilder;

  @Mock private Project project;

  @Mock private Logger logger;

  private static final String GIT_TAG = "v1.0.0";

  @BeforeEach
  void setUp() {
    when(project.getLogger()).thenReturn(logger);
  }

  @Test
  void shouldReturnCommitList() throws Exception {
    when(processBuilder.command()).thenReturn(List.of("git", "log", "v1.0.0..HEAD", "--oneline"));

    Process process = mock(Process.class);

    String output = "abc123 First commit\n" + "def456 Second commit\n";

    when(processBuilder.start()).thenReturn(process);
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream(output.getBytes()));
    when(process.waitFor()).thenReturn(0);

    CommitsSinceTag service = new CommitsSinceTag(processBuilder, project, GIT_TAG);

    List<String> commits = service.retrieve();

    assertEquals(List.of("abc123 First commit", "def456 Second commit"), commits);
  }

  @Test
  void shouldReturnEmptyListWhenNoCommitsFound() throws Exception {
    when(processBuilder.command()).thenReturn(List.of("git", "log", "v1.0.0..HEAD", "--oneline"));

    Process process = mock(Process.class);

    when(processBuilder.start()).thenReturn(process);
    when(process.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(process.waitFor()).thenReturn(0);

    CommitsSinceTag service = new CommitsSinceTag(processBuilder, project, GIT_TAG);

    List<String> commits = service.retrieve();

    assertEquals(List.of(), commits);
  }

  @Test
  void shouldFailWhenGitCommandExitCodeIsNonZero() throws Exception {
    when(processBuilder.command()).thenReturn(List.of("git", "log", "v1.0.0..HEAD", "--oneline"));

    Process process = mock(Process.class);

    when(processBuilder.start()).thenReturn(process);
    when(process.getInputStream())
        .thenReturn(new ByteArrayInputStream("abc123 Commit\n".getBytes()));
    when(process.waitFor()).thenReturn(1);

    CommitsSinceTag service = new CommitsSinceTag(processBuilder, project, GIT_TAG);

    assertThrows(RuntimeException.class, service::retrieve);
  }

  @Test
  void shouldFailWhenProcessBuilderThrowsIOException() throws Exception {
    when(processBuilder.command()).thenReturn(List.of("git", "log", "v1.0.0..HEAD", "--oneline"));

    when(processBuilder.start()).thenThrow(new IOException("git not found"));

    CommitsSinceTag service = new CommitsSinceTag(processBuilder, project, GIT_TAG);

    assertThrows(Upcoming4jException.class, service::retrieve);
  }

  @Test
  void shouldFailWhenGitLogIsInterrupted() throws Exception {
    when(processBuilder.command()).thenReturn(List.of("git", "log", "v1.0.0..HEAD", "--oneline"));

    Process process = mock(Process.class);

    when(processBuilder.start()).thenReturn(process);
    when(process.getInputStream())
        .thenReturn(new ByteArrayInputStream("abc123 Commit\n".getBytes()));
    when(process.waitFor()).thenThrow(new InterruptedException());

    CommitsSinceTag service = new CommitsSinceTag(processBuilder, project, GIT_TAG);

    assertThrows(Upcoming4jException.class, service::retrieve);
  }
}
