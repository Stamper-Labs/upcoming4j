package io.stamperlabs.upcoming4j.process;

import java.io.File;

public class GitProcessBuilderFactory {

  private GitProcessBuilderFactory() {}

  public static ProcessBuilder fetchTags(File workingDir) {
    return new ProcessBuilder("git", "fetch", "--tags", "--prune").directory(workingDir);
  }

  public static ProcessBuilder latestTag(File workingDir) {
    return new ProcessBuilder(
            "git",
            "for-each-ref",
            "--sort=-creatordate",
            "--count=1",
            "--format=%(refname:short)",
            "refs/tags")
        .directory(workingDir);
  }

  public static ProcessBuilder logSinceTag(File workingDir, String tag) {
    return new ProcessBuilder("git", "log", tag + "..HEAD", "--pretty=format:%s")
        .directory(workingDir);
  }
}
