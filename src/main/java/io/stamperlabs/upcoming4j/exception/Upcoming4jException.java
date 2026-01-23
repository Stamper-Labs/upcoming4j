package io.stamperlabs.upcoming4j.exception;

import org.gradle.api.GradleException;

public class Upcoming4jException extends GradleException {
  public Upcoming4jException(String message, Throwable t) {
    super(message, t);
  }

  public Upcoming4jException(String message) {
    super(message);
  }
}
