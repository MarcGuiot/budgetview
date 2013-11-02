package com.designup.siteweaver.generation.utils;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.FileOutput;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.CopySet;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;
import junit.framework.TestCase;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public abstract class GeneratorTestCase extends TestCase {
  protected Page rootPage = new Page("root.html", "root", "root$");
  private Map<String,Page> pages = new HashMap<String, Page>();
  protected DummyFormatter formatter = new DummyFormatter();
  protected Site site;
  protected HtmlOutput output = new FileOutput("tmp/output");

  protected abstract Generator getGenerator();

  public void setUp() {
    site = new Site(rootPage, "tmp/input", "", "", "remote", new ArrayList<CopySet>());
    for (int i = 1; i < 4; i++) {
      Page lev1page = createPage("p" + i, rootPage);
      for (int j = 1; j < 4; j++) {
        Page lev2page = createPage("p" + i + "_" + j, lev1page);
        for (int k = 1; k < 4; k++) {
          createPage("p" + i + "_" + j + "_" + k, lev2page);
        }
      }
    }
  }

  private Page createPage(String pageName, Page parentPage) {
    Page childPage = new Page(pageName + ".html", pageName, pageName + "$");
    parentPage.addSubPage(childPage);
    pages.put(pageName, childPage);
    return childPage;
  }

  protected void checkOutput(String pagePath, String expectedOutput) throws IOException {
    Page targetPage = pagePath.equals("") ? rootPage : pages.get(pagePath);
    assertEquals(expectedOutput, generate(targetPage));
  }

  protected void checkOutput(Page targetPage, String expectedOutput) throws IOException {
    assertEquals(expectedOutput, generate(targetPage));
  }

  protected String generate(Page targetPage) throws IOException {
    StringWriter writer = new StringWriter();
    getGenerator().processPage(site, targetPage, new HtmlWriter(writer), output);
    return writer.toString();
  }

  protected File dumpHtmlToFile(String path, String content) throws IOException {
    File file = new File(path);
    file.getParentFile().mkdirs();
    Writer writer = new FileWriter(file);
    writer.write(
      "<html>" +
      "  <head>" +
      "    <title>Contact</title>" +
      "    <meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\">" +
      "  </head>" +
      "<body bgcolor=\"#FFFFFF\">" +
      content +
      "</body>" +
      "</html>"
    );
    writer.close();
    return file;
  }
}
