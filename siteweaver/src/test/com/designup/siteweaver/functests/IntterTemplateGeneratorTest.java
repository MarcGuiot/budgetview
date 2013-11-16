package com.designup.siteweaver.functests;

import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.xml.SiteParser;

import java.io.File;

public class IntterTemplateGeneratorTest extends SiteweaverTestCase {
  public void testGenTagsCanBeIntegratedInAPageContentFile() throws Exception {
    String descriptor =
      "<site url='http://www.siteweaver.org'>" +
      "  <page file='index.html' title='Home' template='template.html'>" +
      "    <key name='innerTemplate' value='innerTemplate.html'/>" +
      "    <page file='page1.html' title='Page1'>" +
      "      <key name='innerTemplate' value='innerTemplate1.html'/>" +
      "      <page file='page1/page1a.html' title='Page1a'/>" +
      "      <page file='page1/page1b.html' title='Page1b'/>" +
      "    </page>" +
      "    <page file='page2.html' title='Page2'/>" +
      "  </page>" +
      "</site>";

    File configFile = dump("siteweaver.xml", descriptor);
    Site site = SiteParser.parse(configFile);
    dump("index.html", "index");
    dump("page1.html", "content for page1");
    dump("page1/page1a.html", "content for page1a");
    dump("page1/page1b.html", "content for page1b");
    dump("page2.html", "content for page2");

    dump("template.html", "$<gen type=\"innerTemplate\">$");
    dump("innerTemplate.html", "innerTemplate:<gen type=\"content\">");
    dump("innerTemplate1.html", "innerTemplate1:<gen type=\"content\">");

    checkGeneratedPage(site, "index.html", "$innerTemplate:index$");
    checkGeneratedPage(site, "page1.html", "$innerTemplate1:content for page1$");
    checkGeneratedPage(site, "page1/page1a.html", "$innerTemplate1:content for page1a$");
    checkGeneratedPage(site, "page1/page1b.html", "$innerTemplate1:content for page1b$");
    checkGeneratedPage(site, "page2.html", "$innerTemplate:content for page2$");

  }
}
