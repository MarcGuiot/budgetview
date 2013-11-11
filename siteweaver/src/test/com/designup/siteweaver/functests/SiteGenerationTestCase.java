package com.designup.siteweaver.functests;

import com.designup.siteweaver.generation.SiteGenerator;
import com.designup.siteweaver.html.output.FileOutput;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.utils.FileUtils;
import com.designup.siteweaver.xml.SiteParser;

import java.io.File;
import java.io.Reader;

public abstract class SiteGenerationTestCase extends SiteweaverTestCase {

  protected HtmlOutput output = new FileOutput("tmp/output");

  public void setUp() throws Exception {
    super.setUp();
  }

  protected String getLocalPath(String path) {
    return "input/" + path;
  }

  protected void dumpOutput(String path, String content) throws Exception {
    FileUtils.dumpStringToFile(new File(tmpDir, "output/" + path), content);
  }

  protected void generateSite(String xmlDescriptor) throws Exception {
    String fullDescriptor =
      "<site url='file://www.siteweaver.org'>" +
      xmlDescriptor +
      "</site>";

    File configFile = dump("siteweaver.xml", fullDescriptor);
    Site site = SiteParser.parse(configFile);
    SiteGenerator.run(site, output);
  }

  protected void checkOutput(String path, String expectedContent) throws Exception {
    Reader reader = FileUtils.createEncodedReader(getOutputFile(path));
    String actual = FileUtils.readerToString(reader);
    assertEquals(expectedContent, actual);
  }

  protected File getOutputFile(String path) {
    return new File(tmpDir, "output/" + path);
  }}
