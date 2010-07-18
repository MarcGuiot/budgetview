package org.uispec4j.utils;

import java.util.HashSet;
import java.util.Set;

public class ExceptionContainer {
  private StackTraceElement[] stackTraceElements;
  private Set<Throwable> registered = new HashSet<Throwable>();
  private Throwable throwable = null;

  public ExceptionContainer() {
  }

  public ExceptionContainer(RuntimeException callerStack) {
    stackTraceElements = callerStack.getStackTrace();
  }

  public synchronized void set(Throwable e) {
    if (!registered.add(e)) {
      return;
    }
    {
      Throwable subException = e;
      boolean found = false;
      while (subException.getCause() != null) {
        subException = subException.getCause();
        found |= !registered.add(subException);
      }
      if (found){
        return;
      }
    }

    if (throwable != null) {
      Throwable subException = e;
      while (subException.getCause() != null) {
        subException = subException.getCause();
      }
      subException.initCause(throwable);
    }
    throwable = e;
  }

  public boolean isSet() {
    return throwable != null;
  }

  public synchronized void rethrowIfNeeded() {
    try {
      if (throwable == null){
        return;
      }
      if (stackTraceElements != null) {
        completeStackTrace(throwable);
      }
      if (throwable instanceof RuntimeException) {
        throw ((RuntimeException)throwable);
      }
      if (throwable instanceof Error) {
        throw  (Error)throwable;
      }
      throw new RuntimeException(throwable);
    }
    finally {
      reset();
    }
  }

  private void completeStackTrace(Throwable exception) {
    StackTraceElement[] stackTraceElements1 = exception.getStackTrace();
    StackTraceElement[] newStack = new StackTraceElement[stackTraceElements.length + stackTraceElements1.length];
    int i = 0;
    for (StackTraceElement element : stackTraceElements) {
      newStack[i] = element;
      i++;
    }
    for (StackTraceElement element : stackTraceElements1) {
      newStack[i] = element;
      i++;
    }
    exception.setStackTrace(newStack);
  }

  public synchronized void reset() {
    throwable = null;
    registered.clear();
  }
}
