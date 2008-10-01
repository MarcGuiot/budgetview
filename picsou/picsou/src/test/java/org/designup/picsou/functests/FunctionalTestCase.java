package org.designup.picsou.functests;

import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.TestUtils;
import org.uispec4j.UISpec4J;
import org.uispec4j.UISpecTestCase;

import java.io.File;
import java.util.Locale;

public abstract class FunctionalTestCase extends UISpecTestCase {

  static {
    TestUtils.clearTmpDir();
    Locale.setDefault(Locale.ENGLISH);
    UISpec4J.setAssertionTimeLimit(1000);
  }

  protected void setUp() throws Exception {
    super.setUp();
    Locale.setDefault(Locale.ENGLISH);
    Log.reset();
    Files.deleteSubtree(new File(createPrevaylerRepository()));
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    Files.deleteSubtree(new File(createPrevaylerRepository()));
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
