package com.designup.siteweaver.xml;

import com.designup.siteweaver.model.CopySet;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.utils.ArrayUtils;
import junit.framework.TestCase;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

public class SiteParserTest extends TestCase {
  public void testStandardCase() throws Exception {
    Site site = parse(
      "<site url='remote'>" +
      "  <page title='title' shortTitle='short' file='file' template='tpl'/>" +
      "</site>");
    assertEquals("remote", site.getUrl());
    Page rootPage = site.getRootPage();
    assertNotNull(rootPage);
    checkPage(rootPage, "title", "short", "file", "tpl", true);
    assertEquals(0, rootPage.getBorderBoxesFiles().length);
  }

  public void testPagesHierarchy() throws Exception {
    Page root = parsePages(
      "<page title='title' shortTitle='short' file='file' template='tpl'>" +
      "  <page title='title1' shortTitle='shortTitle1' file='file1'>" +
      "     <page title='title11' shortTitle='shortTitle11' file='file11'/>" +
      "  </page>" +
      "  <page title='title2' shortTitle='shortTitle2' file='file2'/>" +
      "</page>");
    checkPage(root, "title", "short", "file", "tpl", true);
    assertEquals(2, root.getSubPagesCount());
    Page page1 = root.getSubPageAtIndex(0);
    checkPage(page1, "title1", "shortTitle1", "file1", "tpl", true);
    assertEquals(1, page1.getSubPagesCount());
    Page page11 = page1.getSubPageAtIndex(0);
    checkPage(page11, "title11", "shortTitle11", "file11", "tpl", true);
    assertEquals(0, page11.getSubPagesCount());
    Page page2 = root.getSubPageAtIndex(1);
    checkPage(page2, "title2", "shortTitle2", "file2", "tpl", true);
    assertEquals(0, page2.getSubPagesCount());
  }

  public void testTheShortTitleIsOptionnal() throws Exception {
    Page root = parsePages("<page title='title' file='file' template='tpl'/>");
    checkPage(root, "title", "title", "file", "tpl", true);
  }

  public void testTemplateGenerationDisabled() throws Exception {
    Page root = parsePages(
      "<page title='title' shortTitle='short' file='file' template='tpl' disableTemplate='true'/>");
    checkPage(root, "title", "short", "file", "tpl", false);
  }

  public void testSubtreeSpecificTemplate() throws Exception {
    Page root = parsePages(
      "<page title='title' shortTitle='short' file='file' template='tpl'>" +
      "  <page title='title1' shortTitle='shortTitle1' file='file1' template='anotherTpl'>" +
      "     <page title='title11' shortTitle='shortTitle11' file='file11'/>" +
      "  </page>" +
      "  <page title='title2' shortTitle='shortTitle2' file='file2'/>" +
      "</page>");
    checkPage(root, "title", "short", "file", "tpl", true);
    assertEquals(2, root.getSubPagesCount());
    Page page1 = root.getSubPageAtIndex(0);
    checkPage(page1, "title1", "shortTitle1", "file1", "anotherTpl", true);
    assertEquals(1, page1.getSubPagesCount());
    Page page11 = page1.getSubPageAtIndex(0);
    checkPage(page11, "title11", "shortTitle11", "file11", "anotherTpl", true);
    assertEquals(0, page11.getSubPagesCount());
    Page page2 = root.getSubPageAtIndex(1);
    checkPage(page2, "title2", "shortTitle2", "file2", "tpl", true);
    assertEquals(0, page2.getSubPagesCount());
  }

  public void testASiteMustHaveExactlyOneRootPage() throws Exception {
    try {
      parse(
        "<site inputDir='input' outputDir='output' localURL='local' remoteURL='remote'>" +
        "</site>");
      fail();
    }
    catch (XmlParsingException e) {
      assertEquals("No <page> tag found under <site>", e.getMessage());
    }
    try {
      parse(
        "<site inputDir='input' outputDir='output' localURL='local' remoteURL='remote'>" +
        "  <page title='Title1' file='file1.txt'/>" +
        "  <page title='Title2' file='file2.txt'/>" +
        "</site>");
      fail();
    }
    catch (XmlParsingException e) {
      assertEquals("Only one <page> can be defined under <site> - actual content:\n" +
                   "  page / title=Title1\n" +
                   "  page / title=Title2\n",
                   e.getMessage());
    }
  }

  public void testKeyValue() throws Exception {
    Page root = parsePages(
      "<page title='title' shortTitle='short' file='file' template='tpl'>" +
      "  <key name='k1' value='v1'/>" +
      "  <key name='k2' value='v2'/>" +
      "</page>");
    assertEquals("v1", root.getValueForKey("k1", false));
    assertEquals("v2", root.getValueForKey("k2", false));
  }

  public void testBorderBoxes() throws Exception {
    Page root = parsePages(
      "<page title='title' shortTitle='short' file='file' template='tpl'>" +
      "  <box file='/var/tmp'/>" +
      "  <box file='/etc/hosts'/>" +
      "</page>");
    ArrayUtils.assertEquals(new String[]{"/var/tmp", "/etc/hosts"}, root.getBorderBoxesFiles());
  }

  public void testFilesToCopy() throws Exception {
    Site site = parse(
      "<site url='remote'>" +
      "  <page title='Title' file='file.txt'/>" +
      "  <copy>" +
      "    <file path='dir/file1.txt'/>" +
      "    <dir  path='dir/subDir'/>" +
      "    <file path='file.txt'/>" +
      "  </copy>" +
      "</site>");
    ArrayUtils.assertSetEquals(new String[]{"dir/file1.txt", "dir/subDir", "file.txt"},
                               toList(site.getCopySets()));
  }

  public void testFilesToCopyWithSeveralRootCopyNodes() throws Exception {
    Site site = parse(
      "<site url='remote'>" +
      "  <page title='Title' file='file.txt'/>" +
      "  <copy>" +
      "    <file path='dir/file1.txt'/>" +
      "  </copy>" +
      "  <copy>" +
      "    <dir  path='dir/subDir'/>" +
      "  </copy>" +
      "  <copy>" +
      "    <file path='file.txt'/>" +
      "  </copy>" +
      "</site>");
    ArrayUtils.assertEquals(new String[]{"dir/file1.txt", "dir/subDir", "file.txt"},
                            toList(site.getCopySets()));
  }

  public void test() throws Exception {
    String chars = "é è à ç ê ù";
    String converted = "&eacute; &egrave; &agrave; &ccedil; &ecirc; &ugrave;";
    Page root = parsePages(
      "<page title='" + chars + "' shortTitle='" + chars + "' file='file' template='tpl'>" +
      "  <key name='k1' value='" + chars + "'/>" +
      "</page>");
    assertEquals(converted, root.getTitle());
    assertEquals(converted, root.getShortTitle());
    assertEquals(converted, root.getValueForKey("k1", false));
  }

  private Site parse(String xml) throws Exception {
    final StringReader configFile = new StringReader(xml.replace('\'', '"'));
    return SiteParser.parse(configFile, "input");
  }

  private String[] toList(List<CopySet> filesToCopy) {
    List<String> result = new ArrayList<String>();
    for (CopySet copySet : filesToCopy) {
      for (String path : copySet.getPaths()) {
        result.add(path);
      }
    }
    return result.toArray(new String[result.size()]);
  }

  private Page parsePages(String xml) throws Exception {
    return parse(
      "<site url='remote'>" +
      xml +
      "</site>").getRootPage();
  }

  private void checkPage(Page page, String title, String shortTitle, String file, String templateFile, boolean templateGenerationEnabled) {
    assertEquals(title, page.getTitle());
    assertEquals(shortTitle, page.getShortTitle());
    assertEquals(file, page.getFileName());
    assertEquals(templateFile, page.getTemplateFile());
    assertEquals(templateGenerationEnabled, page.isTemplateGenerationEnabled());
  }
}
