package com.designup.siteweaver.functests;

import java.io.File;
import java.io.FilenameFilter;

public class FileCopyTest extends SiteweaverTestCase {

  protected void setUp() throws Exception {
    super.setUp();
    dump("tpl.html", "tpl:[<gen type=\"filepath\">]");
  }

  public void testCopyingFiles() throws Exception {
    dump("files/file1.txt", "f1");
    dump("files/file2.txt", "f2");

    generateSite(
      "<page file='root.jpg' title='Root' template='tpl.html'/>" +
      "<copy>" +
      "  <file path='files/file1.txt'/>" +
      "  <file path='files/file2.txt'/>" +
      "</copy>");

    checkOutput("files/file1.txt", "f1");
    checkOutput("files/file2.txt", "f2");
  }

  public void testCopyingDirectories() throws Exception {
    dump("files/file1.txt", "f1");
    dump("files/file2.txt", "f2");

    generateSite(
      "<page file='root.jpg' title='Root' template='tpl.html'/>" +
      "<copy>" +
      "  <dir path='files'/>" +
      "</copy>");

    checkOutput("files/file1.txt", "f1");
    checkOutput("files/file2.txt", "f2");
  }

  public void testExistingFilesAreOverwritten() throws Exception {
    dumpOutput("files/file1.txt", "old");
    dump("files/file1.txt", "new");

    generateSite(
      "<page file='root.jpg' title='Root' template='tpl.html'/>" +
      "<copy>" +
      "  <file path='files/file1.txt'/>" +
      "</copy>");

    checkOutput("files/file1.txt", "new");
  }
}
