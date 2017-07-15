package org.globsframework.utils.exceptions;

public interface ExceptionHandler {
  void onException(Throwable ex);

  void setFirstReset(boolean firstReset);
}
