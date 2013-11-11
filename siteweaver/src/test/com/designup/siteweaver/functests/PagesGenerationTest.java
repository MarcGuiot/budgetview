package com.designup.siteweaver.functests;

public class PagesGenerationTest extends SiteGenerationTestCase {

  public void testStandardCase() throws Exception {
    dump("tpl.html", "tpl:[<gen type=\"content\">]");
    dump("root.html", "<body>rootContent</body>");
    dump("dir1/page1.html", "<body>page1Content</body>");
    dump("dir1/page2.html", "<body>page2Content</body>");

    generateSite(
      "<page file='root.html' title='Root' template='tpl.html'>" +
      "  <page file='dir1/page1.html' title='Page1'/>" +
      "  <page file='dir1/page2.html' title='Page2'/>" +
      "</page>");

    checkOutput("root.html", "tpl:[rootContent]");
    checkOutput("dir1/page1.html", "tpl:[page1Content]");
    checkOutput("dir1/page2.html", "tpl:[page2Content]");
  }

  public void testUsingSeveralTemplates() throws Exception {
    dump("tplA.html", "tplA:[<gen type=\"content\">]");
    dump("tplB.html", "tplB:[<gen type=\"content\">]");
    dump("root.html", "<body>rootContent</body>");
    dump("dir1/page1.html", "<body>page1Content</body>");
    dump("dir1/page11.html", "<body>page11Content</body>");
    dump("dir1/page2.html", "<body>page2Content</body>");

    generateSite(
      "<page file='root.html' title='Root' template='tplA.html'>" +
      "  <page file='dir1/page1.html' title='Page1' template='tplB.html'>" +
      "    <page file='dir1/page11.html' title='Page11'/>" +
      "  </page>" +
      "  <page file='dir1/page2.html' title='Page2'/>" +
      "</page>");

    checkOutput("root.html", "tplA:[rootContent]");
    checkOutput("dir1/page1.html", "tplB:[page1Content]");
    checkOutput("dir1/page11.html", "tplB:[page11Content]");
    checkOutput("dir1/page2.html", "tplA:[page2Content]");
  }

  public void testNonHtmlFilesAreRenamed() throws Exception {
    dump("tpl.html", "tpl:[<gen type=\"filepath\">]");
    dump("root.jpg", "xxx");

    generateSite(
      "<page file='root.jpg' title='Root' template='tpl.html'/>");

    checkOutput("root.html", "tpl:[root.jpg]");
  }

  public void testATemplateMustBeSetForTheRootPage() throws Exception {
    dump("root.html", "<body>rootContent</body>");
    dump("dir1/page1.html", "<body>page1Content</body>");
    dump("dir1/page2.html", "<body>page2Content</body>");

    try {
      generateSite(
        "<page file='root.html' title='Root'>" +
        "  <page file='dir1/page1.html' title='Page1'/>" +
        "  <page file='dir1/page2.html' title='Page2'/>" +
        "</page>");
    }
    catch (Exception e) {
      assertEquals("A template must be provided for the root page", e.getMessage());
    }
  }
}
