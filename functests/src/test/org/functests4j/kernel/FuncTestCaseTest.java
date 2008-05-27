package org.functests4j.kernel;

import junit.framework.TestCase;
import org.functests4j.kernel.FuncTestCmd;
import org.functests4j.kernel.FuncTestCase;
import org.functests4j.kernel.AsynCmd;
import org.functests4j.DefaultFuncTestEvent;

public class FuncTestCaseTest extends TestCase {
  private Object attributeValue;

  public void testExpectedBlockUntilActualArrive() throws Exception {
    FuncTestCase funcTestCase = new FuncTestCase();
    DefaultFuncTestEvent actual = new DefaultFuncTestEvent("eventName");
    callInThread(funcTestCase, actual);
    DefaultFuncTestEvent expected = new DefaultFuncTestEvent("eventName").setReturnValue("ret", "val");
    funcTestCase.expect(expected);
    checkEmpty(funcTestCase);
    assertTrue(funcTestCase.getNotProcessedInfo().length() == 0);
    assertFalse(attributeValue == "val");
  }

  private void checkEmpty(FuncTestCase funcTestCase) {
    String notProcessedInfo = funcTestCase.getNotProcessedInfo();
    assertTrue(notProcessedInfo, notProcessedInfo.length() == 0);
  }

  private void callInThread(final FuncTestCase funcTestCase, final DefaultFuncTestEvent actual) {
    Thread thread = new Thread() {
      public void run() {
        try {
          funcTestCase.actual(actual);
          synchronized (this) {
            attributeValue = actual.getAttributeValue("ret");
          }
        } catch (Exception e) {
        }
      }
    };
    thread.setDaemon(true);
    thread.start();
  }

  public void testAsyncCommand() throws Exception {
    final FuncTestCase funcTestCase = new FuncTestCase();
    AsynCmd asynCmd = funcTestCase.asyncCall(new FuncTestCmd() {
      public void call() throws Exception {
        funcTestCase.actual(new DefaultFuncTestEvent("an Event"));
      }

      public String getDescription() {
        return "anonymous";
      }
    });
    DefaultFuncTestEvent expected = new DefaultFuncTestEvent("an Event");
    funcTestCase.expect(expected);
    checkEmpty(funcTestCase);
    funcTestCase.waitEnd(asynCmd, 1);
  }
}
