package io.stamperlabs.upcoming4j;

import org.gradle.api.Project;
import org.gradle.api.logging.Logger;
import org.gradle.api.tasks.TaskContainer;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

class Upcoming4jPluginTest {

  @Mock private Project project;
  @Mock private TaskContainer tasks;
  @Mock private Logger logger;

  @Test
  void taskLogsCorrectMessage() {}
}
