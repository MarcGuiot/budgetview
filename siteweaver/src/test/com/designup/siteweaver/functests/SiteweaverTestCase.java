package com.designup.siteweaver.functests;

import com.designup.siteweaver.utils.FileUtils;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public abstract class SiteweaverTestCase extends TestCase {

  protected static File tmpDir = new File("tmp");

  protected void setUp() throws Exception {
    super.setUp();
    FileUtils.emptyDirectory(tmpDir);
  }

  protected File dump(String path, String content) throws Exception {
    File file = new File(tmpDir, getLocalPath(path));
    FileUtils.dumpStringToFile(file, content);
    return file;
  }

  protected String getLocalPath(String path) {
    return path;
  }

  protected File dump(File file, String content) throws IOException {
    FileUtils.dumpStringToFile(file, content);
    return file;
  }

}
