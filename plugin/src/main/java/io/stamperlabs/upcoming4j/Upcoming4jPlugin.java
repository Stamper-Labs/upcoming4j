package io.stamperlabs.upcoming4j;

import java.io.IOException;
import java.util.List;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class Upcoming4jPlugin implements Plugin<Project> {

  @Override
  public void apply(Project project) {

    ProcessBuilder gitFetchProcessBuilder = new ProcessBuilder("git", "fetch", "--tags", "--prune");
    ProcessBuilder gitForEachRefProcessBuilder =
        new ProcessBuilder(
            "git",
            "for-each-ref",
            "--sort=-creatordate",
            "--count=1",
            "--format=%(refname:short)",
            "refs/tags");
    gitFetchProcessBuilder.directory(project.getRootDir());
    gitForEachRefProcessBuilder.directory(project.getRootDir());
    var latestGitTagService =
        new LatestGitTag(gitFetchProcessBuilder, gitForEachRefProcessBuilder, project);
    String gitTag = null;
    try {
      gitTag = latestGitTagService.retrieve();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
    if (gitTag == null || gitTag.isBlank()) {
      gitTag = "0.0.0";
    }
    ProcessBuilder gitLogProcessBuilder =
        new ProcessBuilder("git", "log", gitTag + "..HEAD", "--pretty=format:%s");
    gitLogProcessBuilder.directory(project.getRootDir());
    var commitsSinceTagService = new CommitsSinceTag(gitLogProcessBuilder, project, gitTag);
    List<String> commitHistory = null;
    try {
      commitHistory = commitsSinceTagService.retrieve();
    } catch (IOException e) {
      throw new RuntimeException(e);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    var nextVersionService = new NextVersion(project);
    String nextVersion = nextVersionService.compute(gitTag, commitHistory);
    project.getExtensions().getExtraProperties().set("nextVersion", nextVersion);
  }
}
