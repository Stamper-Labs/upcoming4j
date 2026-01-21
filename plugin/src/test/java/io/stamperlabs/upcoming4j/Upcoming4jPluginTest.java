package io.stamperlabs.upcoming4j;

import static org.mockito.Mockito.*;

import java.io.File;
import java.util.List;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionContainer;
import org.gradle.api.plugins.ExtraPropertiesExtension;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

class Upcoming4jPluginTest {

  @Test
  void apply_setsNextVersionExtraProperty() throws Exception {
    Project project = mock(Project.class);
    when(project.getRootDir()).thenReturn(new File("."));
    when(project.getLogger()).thenReturn(mock(org.gradle.api.logging.Logger.class));

    ExtensionContainer extensions = mock(ExtensionContainer.class);
    ExtraPropertiesExtension extraProps = mock(ExtraPropertiesExtension.class);

    when(project.getExtensions()).thenReturn(extensions);
    when(extensions.getExtraProperties()).thenReturn(extraProps);

    try (MockedConstruction<LatestGitTag> latestTagMock =
            mockConstruction(
                LatestGitTag.class, (mock, context) -> when(mock.retrieve()).thenReturn("1.2.3"));
        MockedConstruction<CommitsSinceTag> commitsMock =
            mockConstruction(
                CommitsSinceTag.class,
                (mock, context) ->
                    when(mock.retrieve()).thenReturn(List.of("feat: new feature", "fix: bug")));
        MockedConstruction<NextVersion> nextVersionMock =
            mockConstruction(
                NextVersion.class,
                (mock, context) ->
                    when(mock.compute("1.2.3", List.of("feat: new feature", "fix: bug")))
                        .thenReturn("1.3.0"))) {

      Upcoming4jPlugin plugin = new Upcoming4jPlugin();
      plugin.apply(project);

      verify(extraProps).set("nextVersion", "1.3.0");
    }
  }

  @Test
  void apply_usesDefaultVersionWhenNoGitTag() throws Exception {
    Project project = mock(Project.class);
    when(project.getRootDir()).thenReturn(new File("."));
    when(project.getLogger()).thenReturn(mock(org.gradle.api.logging.Logger.class));

    ExtensionContainer extensions = mock(ExtensionContainer.class);
    ExtraPropertiesExtension extraProps = mock(ExtraPropertiesExtension.class);
    when(project.getExtensions()).thenReturn(extensions);
    when(extensions.getExtraProperties()).thenReturn(extraProps);

    try (MockedConstruction<LatestGitTag> latestTagMock =
            mockConstruction(
                LatestGitTag.class, (mock, context) -> when(mock.retrieve()).thenReturn(""));
        MockedConstruction<CommitsSinceTag> commitsMock =
            mockConstruction(
                CommitsSinceTag.class,
                (mock, context) -> when(mock.retrieve()).thenReturn(List.of("fix: bug")));
        MockedConstruction<NextVersion> nextVersionMock =
            mockConstruction(
                NextVersion.class,
                (mock, context) ->
                    when(mock.compute("0.0.0", List.of("fix: bug"))).thenReturn("0.0.1"))) {

      new Upcoming4jPlugin().apply(project);

      verify(extraProps).set("nextVersion", "0.0.1");
    }
  }
}
