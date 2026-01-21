package io.stamperlabs.upcoming4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import org.gradle.api.Project;

public class LatestGitTag {

  private final ProcessBuilder gitFetchProcessBuilder;
  private final ProcessBuilder gitForEachRefProcessBuilder;
  private final Project project;

  public LatestGitTag(
      ProcessBuilder gitFetchProcessBuilder,
      ProcessBuilder gitForEachRefProcessBuilder,
      Project project) {
    this.gitFetchProcessBuilder = gitFetchProcessBuilder;
    this.gitForEachRefProcessBuilder = gitForEachRefProcessBuilder;
    this.project = project;
  }

  public String retrieve() throws IOException, InterruptedException {
    var logger = project.getLogger();
    logger.lifecycle("Retrieve the latest Git tag");

    logger.lifecycle("Fetch git tags: {}", String.join(" ", gitFetchProcessBuilder.command()));
    var fetchProcess = gitFetchProcessBuilder.start();
    int fetchExitCode = fetchProcess.waitFor();
    if (fetchExitCode != 0) {
      throw new RuntimeException("Git fetch command failed with exit code " + fetchExitCode);
    }

    logger.lifecycle(
        "For each ref tags: {}", String.join(" ", gitForEachRefProcessBuilder.command()));
    var forEachRefProcess = gitForEachRefProcessBuilder.start();
    var reader = new BufferedReader(new InputStreamReader(forEachRefProcess.getInputStream()));

    var line = reader.readLine();
    var latestTag = line != null ? line.trim() : "";

    int forEachRefExitCode = forEachRefProcess.waitFor();
    if (forEachRefExitCode != 0) {
      throw new RuntimeException(
          "Git for-each-ref command failed with exit code " + forEachRefExitCode);
    }

    logger.lifecycle("Latest Git tag found: {}", latestTag);
    return latestTag;
  }
}
