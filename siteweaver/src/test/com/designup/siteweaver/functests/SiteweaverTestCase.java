package com.designup.siteweaver.functests;

import com.designup.siteweaver.generation.SiteGenerator;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.server.utils.LocalOutput;
import com.designup.siteweaver.utils.FileUtils;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

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

  protected void checkGeneratedPage(Site site, String pageFilePath, String expectedOutput) throws IOException {
    StringWriter writer = new StringWriter();
    SiteGenerator.run(site, site.getPageForFile(pageFilePath), new LocalOutput(new PrintWriter(writer)));
    writer.close();
    assertEquals(expectedOutput, writer.toString());
  }
}
