package org.designup.picsou.functests;

import org.designup.picsou.gui.PicsouApplication;
import org.uispec4j.Trigger;
import org.uispec4j.Window;
import org.uispec4j.interception.WindowInterceptor;

public class SingleInstanceTest extends ServerFunctionalTestCase {

  public void test() throws Exception {
    final ApplicationThread[] threads = new ApplicationThread[3];
    for (int i = 0; i < threads.length; i++) {
      threads[i] = new ApplicationThread();
    }
    Window window = WindowInterceptor.run(new Trigger() {
      public void run() throws Exception {
        for (Thread thread : threads) {
          thread.start();
        }
      }
    });
    int errorCount = 0;
    for (ApplicationThread thread : threads) {
      thread.join();
      if (thread.errorReceived) {
        errorCount++;
      }
    }
    assertEquals(0, errorCount);
  }

  private static class ApplicationThread extends Thread {
    boolean errorReceived = false;

    public void run() {
      try {
        PicsouApplication.main();
      }
      catch (Throwable e) {
        errorReceived = true;
      }
    }
  }
}
