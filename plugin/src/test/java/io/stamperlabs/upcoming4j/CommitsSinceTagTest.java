package io.stamperlabs.upcoming4j;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.junit.jupiter.api.Test;

class CommitsSinceTagTest {

  @Test
  void commitsSinceTagReturnsCommitListAndLogs() throws Exception {
    // 1️⃣ Mock the Gradle Project and Logger
    Project project = mock(Project.class);
    Logger logger = mock(Logger.class);
    when(project.getLogger()).thenReturn(logger);

    // 2️⃣ Mock Process and ProcessBuilder
    Process mockedProcess = mock(Process.class);
    ProcessBuilder mockedPB = mock(ProcessBuilder.class);

    String gitOutput = "commit1\ncommit2\ncommit3";
    InputStream stdout = new ByteArrayInputStream(gitOutput.getBytes());

    when(mockedProcess.getInputStream()).thenReturn(stdout);
    when(mockedProcess.waitFor()).thenReturn(0);

    when(mockedPB.start()).thenReturn(mockedProcess);
    when(mockedPB.command())
        .thenReturn(List.of("git", "log", "v1.0.0..HEAD", "--pretty=format:%s"));

    // 3️⃣ Create the CommitsSinceTag instance
    CommitsSinceTag task = new CommitsSinceTag(mockedPB, project, "v1.0.0");

    // 4️⃣ Call the method
    List<String> commits = task.retrieve();

    // 5️⃣ Verify results
    assertNotNull(commits);
    assertEquals(3, commits.size());
    assertEquals("commit1", commits.get(0));
    assertEquals("commit2", commits.get(1));
    assertEquals("commit3", commits.get(2));

    // 6️⃣ Verify logger was called
    verify(logger).lifecycle("Retrieving commits since tag: {}", "v1.0.0");
    verify(logger).lifecycle("Git command: {}", "git log v1.0.0..HEAD --pretty=format:%s");
    verify(logger).lifecycle("Commit --> {}", "commit1");
    verify(logger).lifecycle("Commit --> {}", "commit2");
    verify(logger).lifecycle("Commit --> {}", "commit3");

    // 7️⃣ Verify ProcessBuilder and Process interactions
    verify(mockedPB).start();
    verify(mockedProcess).waitFor();
  }

  @Test
  void commitsSinceTagThrowsWhenProcessFails() throws Exception {
    Project project = mock(Project.class);
    Logger logger = mock(Logger.class);
    when(project.getLogger()).thenReturn(logger);

    Process mockedProcess = mock(Process.class);
    ProcessBuilder mockedPB = mock(ProcessBuilder.class);

    when(mockedProcess.getInputStream()).thenReturn(new ByteArrayInputStream(new byte[0]));
    when(mockedProcess.waitFor()).thenReturn(1); // simulate git failure
    when(mockedPB.start()).thenReturn(mockedProcess);
    when(mockedPB.command())
        .thenReturn(List.of("git", "log", "v1.0.0..HEAD", "--pretty=format:%s"));

    CommitsSinceTag task = new CommitsSinceTag(mockedPB, project, "v1.0.0");

    RuntimeException ex = assertThrows(RuntimeException.class, task::retrieve);
    assertEquals("Git command failed with exit code 1", ex.getMessage());
  }
}
