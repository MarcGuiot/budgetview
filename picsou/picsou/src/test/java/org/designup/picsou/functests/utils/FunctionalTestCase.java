package org.designup.picsou.functests.utils;

import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.TestUtils;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;
import org.uispec4j.utils.DummyRepaintManager;
import org.designup.picsou.functests.utils.LoggedInFunctionalTestCase;
import org.designup.picsou.utils.Lang;

import java.io.File;
import java.util.Locale;

import sun.awt.AWTAutoShutdown;

public abstract class FunctionalTestCase extends UISpecTestCase {
  
  static {
    TestUtils.clearTmpDir();
    Locale.setDefault(Lang.ROOT);
    UISpec4J.setWindowInterceptionTimeLimit(15000);
    UISpec4J.setAssertionTimeLimit(1000);
    DummyRepaintManager.init();
  }

  protected void setUp() throws Exception {
    super.setUp();
    Locale locale = getDefaultLocale();
    Locale.setDefault(locale);
    Lang.setLocale(locale);
    Log.reset();
    AWTAutoShutdown.notifyToolkitThreadBusy();
  }

  protected Locale getDefaultLocale() {
    return Lang.ROOT;
  }

  protected void tearDown() throws Exception {
    super.tearDown();
//    System.gc();
//    long freeMem = Runtime.getRuntime().freeMemory();
//    long maxMem = Runtime.getRuntime().maxMemory();
//    long totalMem = Runtime.getRuntime().totalMemory();
//    System.out.println("FunctionalTestCase.tearDown " + freeMem  + " " + maxMem + " " + totalMem);
  }

  protected static String createPrevaylerRepository() {
    String name = TestUtils.TMP_DIR + "/test_prevayler";
    File file = new File(name);
    if (!file.exists()) {
      file.mkdirs();
    }
    return name;
  }

  public static String getUrl() {
    return createPrevaylerRepository();
  }
}
