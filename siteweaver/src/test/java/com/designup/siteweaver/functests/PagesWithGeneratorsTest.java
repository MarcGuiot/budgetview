package com.designup.siteweaver.functests;

import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.xml.SiteParser;

import java.io.File;

public class PagesWithGeneratorsTest extends SiteweaverTestCase {
  public void testGenTagsCanBeIntegratedInAPageContentFile() throws Exception {
    String descriptor =
      "<site url='http://www.siteweaver.org'>" +
      "  <page file='index.html' title='Home' template='template.html'>" +
      "    <page file='page1.html' title='Page1'>" +
      "      <page file='page1/page1a.html' title='Page1a'/>" +
      "      <page file='page1/page1b.html' title='Page1b'/>" +
      "    </page>" +
      "    <page file='page2.html' title='Page2'/>" +
      "  </page>" +
      "</site>";

    File configFile = dump("siteweaver.xml", descriptor);
    Site site = SiteParser.parse(configFile);
    dump("index.html", "Index");
    dump("page1.html",
         "Start\n" +
         "<gen type=\"title\">\n" +
         "End");
    dump("page1/page1a.html", "content for page1a");
    dump("page1/page1b.html", "content for page1b");

    dump("template.html", "$<gen type=\"content\">$");

    checkGeneratedPage(site, "page1.html",
                       "$Start\n" +
                       "<title>Page1</title>\n\n" +
                       "End$");

  }
}
