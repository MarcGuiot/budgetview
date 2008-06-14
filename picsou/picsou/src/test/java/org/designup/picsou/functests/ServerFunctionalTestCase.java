package org.designup.picsou.functests;

import junit.framework.TestCase;
import org.designup.picsou.functests.utils.FunctionalTestCase;
import org.designup.picsou.server.ServerDirectory;
import org.globsframework.utils.Files;
import org.globsframework.utils.TestUtils;
import org.globsframework.utils.directory.Directory;

import java.io.File;

public abstract class ServerFunctionalTestCase extends FunctionalTestCase {
  protected String url;
  protected Directory directory;
  private ServerDirectory serverDirectory;

  protected void setUp() throws Exception {
    super.setUp();
    Files.deleteSubtree(new File(createPrevaylerRepository()));
    url = initServerEnvironment(this);
  }

  protected void tearDown() throws Exception {
    super.tearDown();
    serverDirectory.close();
    directory = null;
    url = null;
    Files.deleteSubtree(new File(createPrevaylerRepository()));
  }

  public String initServerEnvironment(TestCase testCase) throws Exception {
    String prevaylerPath = createPrevaylerRepository();
    serverDirectory = new ServerDirectory(prevaylerPath, true);
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
