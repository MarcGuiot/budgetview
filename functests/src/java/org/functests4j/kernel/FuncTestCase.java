package org.functests4j.kernel;

import junit.framework.AssertionFailedError;
import org.functests4j.DefaultFuncTestEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class FuncTestCase {

  private final List actualEvents = new ArrayList();

  public void expect(FuncTestEvent expected) throws Exception {
    waitFor(new FuncTestEventState(expected), 1);
  }

  public void actual(DefaultFuncTestEvent actual) throws Exception {
    FuncTestEventState funcTestEventState = new FuncTestEventState(actual);
    synchronized (actualEvents) {
      actualEvents.add(funcTestEventState);
      actualEvents.notify();
    }
    passiveWait(funcTestEventState, 1);
    synchronized (actualEvents) {
      actualEvents.remove(funcTestEventState);
    }
  }

  public String getNotProcessedInfo() {
    StringBuffer stringBuffer = new StringBuffer();
    synchronized (actualEvents) {
      for (Iterator iterator = actualEvents.iterator(); iterator.hasNext();) {
        FuncTestEventState funcTestEventState = (FuncTestEventState)iterator.next();
        if (!funcTestEventState.processed) {
          stringBuffer.append(funcTestEventState.toString()).append("; ");
        }
      }
    }
    return stringBuffer.toString();
  }

  public void call(FuncTestCmd funcTestCmd) throws Exception {
    funcTestCmd.call();
  }

  public AsynCmd asyncCall(final FuncTestCmd funcTestCmd) {
    return new AsynCmd(funcTestCmd).start();
  }

  public boolean waitEnd(AsynCmd asynCmd, int timeoutInSecond) throws Exception {
    DurationWaiter duration = DurationWaiter.init(timeoutInSecond);
    synchronized (asynCmd) {
      while (!asynCmd.isDone() && duration.shouldContinue()) {
        asynCmd.wait(duration.getDuration());
      }
    }
    return asynCmd.isDone();
  }

  private void waitFor(FuncTestEventState eventToFindState, int timeoutInSecond) throws Exception {
    DurationWaiter duration = DurationWaiter.init(timeoutInSecond);
    synchronized (actualEvents) {
      while (eventToFindState.wasNotProccesed() && duration.shouldContinue()) {
        for (Iterator iterator = actualEvents.iterator(); iterator.hasNext();) {
          FuncTestEventState eventState = (FuncTestEventState)iterator.next();
          synchronized (eventState) {
            if (eventState.ifEquivalentMarkAsProccesed(eventToFindState)) {
              return;
            }
          }
        }
        actualEvents.wait(duration.getDuration());
      }
    }
    eventToFindState.failIfNotProccesed();
  }

  static private void passiveWait(FuncTestEventState eventState, int timeoutInSecond) throws Exception {
    DurationWaiter duration = DurationWaiter.init(timeoutInSecond);
    synchronized (eventState) {
      while (eventState.wasNotProccesed() && duration.shouldContinue()) {
        eventState.wait(duration.getDuration());
      }
      eventState.failIfNotProccesed();
    }
  }

  static class FuncTestEventState {
    private FuncTestEvent expected;
    private boolean processed = false;
    private boolean failed = false;

    public FuncTestEventState(FuncTestEvent expected) {
      this.expected = expected;
    }

    synchronized public boolean wasNotProccesed() {
      return !processed && !failed;
    }

    synchronized public boolean ifEquivalentMarkAsProccesed(FuncTestEventState eventToFindState) {
      if (failed) {
        throw new AssertionFailedError();
      }
      synchronized (eventToFindState) {
        if (eventToFindState.expected.isEquivalent(expected)) {
          eventToFindState.expected.setHomoEvent(expected);
          expected.setHomoEvent(eventToFindState.expected);
          eventToFindState.processed = true;
          processed = true;
          eventToFindState.notify();
          return true;
        }
      }
      return false;
    }

    synchronized public void failIfNotProccesed() {
      if (!processed) {
        failed = true;
        processed = true;
      }
    }

    public String toString() {
      return expected.toString();
    }
  }
}
