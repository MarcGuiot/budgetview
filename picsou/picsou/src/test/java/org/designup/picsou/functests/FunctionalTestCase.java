package org.designup.picsou.functests;

import junit.framework.TestCase;
import org.designup.picsou.server.ServerDirectory;
import org.globsframework.utils.Files;
import org.globsframework.utils.Log;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.directory.Directory;
import org.uispec4j.UISpecTestCase;

import java.io.File;
import java.util.Locale;

public abstract class FunctionalTestCase extends UISpecTestCase {
  protected String url;
  protected Directory directory;
  private ServerDirectory serverDirectory;
  private boolean inMemory = true;

  static {
    TestUtils.clearTmpDir();
    Locale.setDefault(Locale.ENGLISH);
  }

  protected void setUp() throws Exception {
    super.setUp();
    Locale.setDefault(Locale.ENGLISH);
    Log.reset();
    Files.deleteSubtree(new File(createPrevaylerRepository()));
    url = initServerEnvironment(this, inMemory);
  }

  protected void setInMemory(boolean inMemory) {
    this.inMemory = inMemory;
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    serverDirectory.close();
    serverDirectory = null;
    directory = null;
    url = null;
    Files.deleteSubtree(new File(createPrevaylerRepository()));
  }

  public String initServerEnvironment(TestCase testCase, boolean inMemory) throws Exception {
    String prevaylerPath = createPrevaylerRepository();
    serverDirectory = new ServerDirectory(prevaylerPath, inMemory);
    directory = serverDirectory.getServiceDirectory();
    return prevaylerPath;
  }

  private static String createPrevaylerRepository() {
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
