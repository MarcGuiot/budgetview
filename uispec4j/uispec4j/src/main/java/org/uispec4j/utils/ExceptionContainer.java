package org.uispec4j.utils;

public class ExceptionContainer {
  private RuntimeException callerStack;
  private RuntimeException exception;
  private Error error;

  public ExceptionContainer() {
  }

  public ExceptionContainer(RuntimeException callerStack) {
    this.callerStack = callerStack;
  }

  public void set(Throwable e) {
    if (e instanceof RuntimeException) {
      exception = (RuntimeException)e;
    }
    else if (e instanceof Error) {
      error = (Error)e;
    }
    else {
      exception = new RuntimeException(e);
    }
  }

  public boolean isSet() {
    return (exception != null) || (error != null);
  }

  public void rethrowIfNeeded() {
    try {
      if (error != null) {
        if (callerStack != null) {
          callerStack.initCause(error);
          throw callerStack;
        }
        throw error;
      }
      if (exception != null) {
        if (callerStack != null) {
          callerStack.initCause(exception);
          throw callerStack;
        }
        throw exception;
      }
    }
    finally {
      reset();
    }
  }

  public void reset() {
    exception = null;
    error = null;
    if (callerStack != null) {
      callerStack = null; //it is not possible to call initCause twice
    }
  }
}
