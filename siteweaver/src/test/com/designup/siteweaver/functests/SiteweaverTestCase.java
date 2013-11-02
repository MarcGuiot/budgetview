package com.designup.siteweaver.functests;

import com.designup.siteweaver.generation.SiteGenerator;
import com.designup.siteweaver.html.output.FileOutput;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.utils.FileUtils;
import com.designup.siteweaver.xml.SiteParser;
import junit.framework.TestCase;

import java.io.File;
import java.io.Reader;
import java.io.StringReader;

public abstract class SiteweaverTestCase extends TestCase {

  private static File tmpDir = new File(System.getProperty("user.dir") + "/tmp");
  protected HtmlOutput output = new FileOutput("tmp/output");

  protected void setUp() throws Exception {
    super.setUp();
    FileUtils.emptyDirectory(tmpDir);
  }

  protected void dump(String path, String content) throws Exception {
    FileUtils.dumpStringToFile(new File(tmpDir, "input/" + path), content);
  }

  protected void dumpOutput(String path, String content) throws Exception {
    FileUtils.dumpStringToFile(new File(tmpDir, "output/" + path), content);
  }

  protected void generateSite(String xmlDescriptor) throws Exception {
    String fullDescriptor =
      "<site url='file://www.siteweaver.org'>" +
      xmlDescriptor +
      "</site>";
    final StringReader configFile = new StringReader(fullDescriptor);
    Site site = SiteParser.parse(configFile, "tmp/input");
    SiteGenerator.run(site, output);
  }

  protected void checkOutput(String path, String expectedContent) throws Exception {
    Reader reader = FileUtils.createEncodedReader(getOutputFile(path));
    String actual = FileUtils.readerToString(reader);
    assertEquals(expectedContent, actual);
  }

  protected File getOutputFile(String path) {
    return new File(tmpDir, "output/" + path);
  }
}
