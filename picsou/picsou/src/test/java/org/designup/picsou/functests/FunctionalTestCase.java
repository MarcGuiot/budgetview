package org.designup.picsou.functests;

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
    UISpec4J.setAssertionTimeLimit(1000);
    DummyRepaintManager.init();
  }

  protected void setUp() throws Exception {
    super.setUp();
    Locale.setDefault(Lang.ROOT);
    Log.reset();
    AWTAutoShutdown.notifyToolkitThreadBusy();
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
