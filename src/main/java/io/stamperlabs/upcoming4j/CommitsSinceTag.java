package io.stamperlabs.upcoming4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import org.gradle.api.Project;

public class CommitsSinceTag {

  private String gitTag;
  private ProcessBuilder processBuilder;
  private Project project;

  public CommitsSinceTag(ProcessBuilder processBuilder, Project project, String gitTag) {
    this.processBuilder = processBuilder;
    this.project = project;
    this.gitTag = gitTag;
  }

  public List<String> retrieve() throws IOException, InterruptedException {
    var logger = project.getLogger();
    logger.lifecycle("Retrieving commits since tag: {}", gitTag);
    logger.lifecycle("Git command: {}", String.join(" ", processBuilder.command()));
    Process process = processBuilder.start();
    var reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
    var commits = reader.lines().toList();
    commits.forEach(commit -> logger.lifecycle("Commit --> {}", commit));
    int exitCode = process.waitFor();
    if (exitCode != 0) {
      throw new RuntimeException("Git command failed with exit code " + exitCode);
    }
    return commits;
  }
}
