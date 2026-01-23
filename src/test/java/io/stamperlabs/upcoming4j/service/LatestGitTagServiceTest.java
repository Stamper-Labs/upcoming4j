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
class LatestGitTagServiceTest {

  @Mock private ProcessBuilder gitFetchProcessBuilder;

  @Mock private ProcessBuilder gitForEachRefProcessBuilder;

  @Mock private Project project;

  @Mock private Logger logger;

  @BeforeEach
  void setUp() {
    when(project.getLogger()).thenReturn(logger);
  }

  @Test
  void shouldReturnLatestGitTag() throws Exception {
    when(gitFetchProcessBuilder.command()).thenReturn(List.of("git", "fetch", "--tags"));
    when(gitForEachRefProcessBuilder.command())
        .thenReturn(List.of("git", "for-each-ref", "--sort=-creatordate"));

    Process fetchProcess = mock(Process.class);
    Process tagProcess = mock(Process.class);

    when(gitFetchProcessBuilder.start()).thenReturn(fetchProcess);
    when(fetchProcess.waitFor()).thenReturn(0);

    when(gitForEachRefProcessBuilder.start()).thenReturn(tagProcess);
    when(tagProcess.getInputStream()).thenReturn(new ByteArrayInputStream("v1.2.3\n".getBytes()));
    when(tagProcess.waitFor()).thenReturn(0);

    LatestGitTagService service =
        new LatestGitTagService(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    String result = service.retrieve();

    assertEquals("v1.2.3", result);
  }

  @Test
  void shouldReturnFallbackVersionWhenNoTagsFound() throws Exception {
    when(gitFetchProcessBuilder.command()).thenReturn(List.of("git", "fetch", "--tags"));
    when(gitForEachRefProcessBuilder.command())
        .thenReturn(List.of("git", "for-each-ref", "--sort=-creatordate"));

    Process fetchProcess = mock(Process.class);
    Process tagProcess = mock(Process.class);

    when(gitFetchProcessBuilder.start()).thenReturn(fetchProcess);
    when(fetchProcess.waitFor()).thenReturn(0);

    when(gitForEachRefProcessBuilder.start()).thenReturn(tagProcess);
    when(tagProcess.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(tagProcess.waitFor()).thenReturn(0);

    LatestGitTagService service =
        new LatestGitTagService(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    String result = service.retrieve();

    assertEquals("0.0.1", result);
  }

  @Test
  void shouldFailWhenGitFetchExitCodeIsNonZero() throws Exception {
    when(gitFetchProcessBuilder.command()).thenReturn(List.of("git", "fetch", "--tags"));

    Process fetchProcess = mock(Process.class);

    when(gitFetchProcessBuilder.start()).thenReturn(fetchProcess);
    when(fetchProcess.waitFor()).thenReturn(1);

    LatestGitTagService service =
        new LatestGitTagService(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    assertThrows(Upcoming4jException.class, service::retrieve);
  }

  @Test
  void shouldFailWhenGitFetchThrowsIOException() throws Exception {
    when(gitFetchProcessBuilder.command()).thenReturn(List.of("git", "fetch", "--tags"));

    when(gitFetchProcessBuilder.start()).thenThrow(new IOException("git not found"));

    LatestGitTagService service =
        new LatestGitTagService(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    assertThrows(Upcoming4jException.class, service::retrieve);
  }

  @Test
  void shouldFailWhenGitFetchIsInterrupted() throws Exception {
    when(gitFetchProcessBuilder.command()).thenReturn(List.of("git", "fetch", "--tags"));

    Process fetchProcess = mock(Process.class);

    when(gitFetchProcessBuilder.start()).thenReturn(fetchProcess);
    when(fetchProcess.waitFor()).thenThrow(new InterruptedException());

    LatestGitTagService service =
        new LatestGitTagService(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    assertThrows(Upcoming4jException.class, service::retrieve);
  }

  @Test
  void shouldFailWhenForEachRefExitCodeIsNonZero() throws Exception {
    when(gitFetchProcessBuilder.command()).thenReturn(List.of("git", "fetch", "--tags"));
    when(gitForEachRefProcessBuilder.command())
        .thenReturn(List.of("git", "for-each-ref", "--sort=-creatordate"));

    Process fetchProcess = mock(Process.class);
    Process tagProcess = mock(Process.class);

    when(gitFetchProcessBuilder.start()).thenReturn(fetchProcess);
    when(fetchProcess.waitFor()).thenReturn(0);

    when(gitForEachRefProcessBuilder.start()).thenReturn(tagProcess);
    when(tagProcess.getInputStream()).thenReturn(new ByteArrayInputStream("v1.0.0\n".getBytes()));
    when(tagProcess.waitFor()).thenReturn(1);

    LatestGitTagService service =
        new LatestGitTagService(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    assertThrows(Upcoming4jException.class, service::retrieve);
  }

  @Test
  void shouldFailWhenForEachRefThrowsIOException() throws Exception {
    when(gitFetchProcessBuilder.command()).thenReturn(List.of("git", "fetch", "--tags"));
    when(gitForEachRefProcessBuilder.command())
        .thenReturn(List.of("git", "for-each-ref", "--sort=-creatordate"));

    Process fetchProcess = mock(Process.class);

    when(gitFetchProcessBuilder.start()).thenReturn(fetchProcess);
    when(fetchProcess.waitFor()).thenReturn(0);

    when(gitForEachRefProcessBuilder.start()).thenThrow(new IOException("git error"));

    LatestGitTagService service =
        new LatestGitTagService(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    assertThrows(Upcoming4jException.class, service::retrieve);
  }

  @Test
  void shouldFailWhenForEachRefIsInterrupted() throws Exception {
    when(gitFetchProcessBuilder.command()).thenReturn(List.of("git", "fetch", "--tags"));
    when(gitForEachRefProcessBuilder.command())
        .thenReturn(List.of("git", "for-each-ref", "--sort=-creatordate"));

    Process fetchProcess = mock(Process.class);
    Process tagProcess = mock(Process.class);

    when(gitFetchProcessBuilder.start()).thenReturn(fetchProcess);
    when(fetchProcess.waitFor()).thenReturn(0);

    when(gitForEachRefProcessBuilder.start()).thenReturn(tagProcess);
    when(tagProcess.getInputStream()).thenReturn(new ByteArrayInputStream("v1.0.0\n".getBytes()));
    when(tagProcess.waitFor()).thenThrow(new InterruptedException());

    LatestGitTagService service =
        new LatestGitTagService(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);

    assertThrows(Upcoming4jException.class, service::retrieve);
  }
}
