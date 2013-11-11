package com.designup.siteweaver.server.upload;

import com.designup.siteweaver.functests.SiteweaverTestCase;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.PageFunctor;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.xml.SiteParser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SiteUploaderTest extends SiteweaverTestCase {

  protected void setUp() throws Exception {
    super.setUp();
  }

  public void testSimpleSite() throws Exception {

    String descriptor =
      "<site url='http://www.siteweaver.org' pagesDir='content'>" +
      "  <page file='index.html' title='Home' template='template.html'>" +
      "    <page file='page1.html' title='Page1'>" +
      "      <page file='page1/page1a.html' title='Page1a'/>" +
      "      <page file='page1/page1b.html' title='Page1b'/>" +
      "    </page>" +
      "    <page file='page2.html' title='Page2'/>" +
      "  </page>" +
      "</site>";

    Site site = createAndDumpSite("dir/siteweaver.xml", descriptor);

    File template = dump("dir/template.html", "$<gen type=\"content\">");

    DummyFileAccess fileAccess =
      DummyFileAccess.init(template.lastModified())
        .addPast("/index.html")
        .addFuture("/page1.html")
        .addPast("/page1/page1b.html")
        .addPast("/page1/page1c.html");

    checkUpload(site, fileAccess,
                "update text: /index.html ==> $=Home\n" +
                "update text: /page1/page1a.html ==> $=Page1a\n" +
                "update text: /page1/page1b.html ==> $=Page1b\n" +
                "update text: /page2.html ==> $=Page2\n" +
                "delete file: /page1/page1c.html\n" +
                "complete\n");
  }

  public void testResources() throws Exception {

    String descriptor =
      "<site url='http://www.siteweaver.org' pagesDir='content'>" +
      "  <page file='index.html' title='Home' template='template.html'>" +
      "    <page file='page1.html' title='Page1'/>" +
      "    <page file='page2.html' title='Page2'/>" +
      "  </page>" +
      "  <copy baseDir='resources'>" +
      "    <file path='css/styles.css'/>" +
      "    <file path='js'/>" +
      "  </copy>" +
      "</site>";
    final Site site = createAndDumpSite("dir/siteweaver.xml", descriptor);

    dump("dir/resources/css/styles.css", "body { color: white; }");
    dump("dir/resources/js/scripts.js", "script");
    File template = dump("dir/template.html", "$<gen type=\"content\">");

    DummyFileAccess fileAccess =
      DummyFileAccess.init(template.lastModified())
        .addPast("/index.html")
        .addFuture("/page1.html")
        .addPast("/page1/page1b.html")
        .addPast("/other/otherPage.html")
        .addFuture("/resources/other.jpg");

    checkUpload(site, fileAccess,
                "update text: /index.html ==> $=Home\n" +
                "update text: /page2.html ==> $=Page2\n" +
                "update file: /css/styles.css ==> upload from /dir/resources/css/styles.css\n" +
                "update file: /js/scripts.js ==> upload from /dir/resources/js/scripts.js\n" +
                "delete file: /other/otherPage.html\n" +
                "delete file: /resources/other.jpg\n" +
                "delete file: /page1/page1b.html\n" +
                "complete\n");
  }

  public void testAllSubtreeIsUploadedForResourceDirs() throws Exception {
    String descriptor =
      "<site url='http://www.siteweaver.org' pagesDir='content'>" +
      "  <page file='index.html' title='Home' template='template.html'>" +
      "    <page file='page1.html' title='Page1'/>" +
      "    <page file='page2.html' title='Page2'/>" +
      "  </page>" +
      "  <copy>" +
      "    <file path='files'/>" +
      "  </copy>" +
      "</site>";
    final Site site = createAndDumpSite("dir/siteweaver.xml", descriptor);

    File template = dump("dir/template.html", "$<gen type=\"content\">");

    dump("dir/files/file1.txt", "file1");
    dump("dir/files/file1.txt", "file2");
    dump("dir/files/subdir/file3.txt", "file3");
    dump("dir/files/subdir/subsubdir/file4.txt", "file3");

    DummyFileAccess fileAccess =
      DummyFileAccess.init(template.lastModified())
        .addPast("/index.html")
        .addFuture("/page1.html")
        .addPast("/page1/page1b.html")
        .addPast("/other/otherPage.html")
        .addFuture("/resources/other.jpg");

    checkUpload(site, fileAccess,
                "update text: /index.html ==> $=Home\n" +
                "update text: /page2.html ==> $=Page2\n" +
                "update file: /files/file1.txt ==> upload from /dir/files/file1.txt\n" +
                "update file: /files/subdir/file3.txt ==> upload from /dir/files/subdir/file3.txt\n" +
                "update file: /files/subdir/subsubdir/file4.txt ==> upload from /dir/files/subdir/subsubdir/file4.txt\n" +
                "delete file: /other/otherPage.html\n" +
                "delete file: /resources/other.jpg\n" +
                "delete file: /page1/page1b.html\n" +
                "complete\n");
  }

  public void testIgnoresDSStoreFiles() throws Exception {
    String descriptor =
      "<site url='http://www.siteweaver.org' pagesDir='content'>" +
      "  <page file='index.html' title='Home' template='template.html'>" +
      "    <page file='page1.html' title='Page1'/>" +
      "    <page file='page2.html' title='Page2'/>" +
      "  </page>" +
      "  <copy>" +
      "    <file path='files'/>" +
      "  </copy>" +
      "</site>";
    final Site site = createAndDumpSite("dir/siteweaver.xml", descriptor);

    File template = dump("dir/template.html", "$<gen type=\"content\">");

    dump("dir/files/file1.txt", "file1");
    dump("dir/files/file2.txt", "file2");
    dump("dir/files/.DS_Store", "xxx");
    dump("dir/files/subdir/file3.txt", "file3");
    dump("dir/files/subdir/.DS_Store", "xxx");
    dump("dir/files/subdir/subsubdir/.DS_Store", "file3");
    dump("dir/files/subdir/subsubdir/file4.txt", "file3");

    DummyFileAccess fileAccess =
      DummyFileAccess.init(template.lastModified())
        .addFuture("/index.html")
        .addFuture("/page1.html")
        .addFuture("/page2.html");

    checkUpload(site, fileAccess,
                "update file: /files/file1.txt ==> upload from /dir/files/file1.txt\n" +
                "update file: /files/file2.txt ==> upload from /dir/files/file2.txt\n" +
                "update file: /files/subdir/file3.txt ==> upload from /dir/files/subdir/file3.txt\n" +
                "update file: /files/subdir/subsubdir/file4.txt ==> upload from /dir/files/subdir/subsubdir/file4.txt\n" +
                "complete\n");
  }

  public void testReloadsAllHtmlInCaseOfConfigFileChange() throws Exception {
    String descriptor =
      "<site url='http://www.siteweaver.org' pagesDir='content'>" +
      "  <page file='index.html' title='Home' template='template.html'>" +
      "    <page file='page1.html' title='Page1'/>" +
      "    <page file='page2.html' title='Page2'/>" +
      "  </page>" +
      "  <copy baseDir='resources'>" +
      "    <file path='css/styles.css'/>" +
      "  </copy>" +
      "</site>";

    File configFile = dump("dir/siteweaver.xml", descriptor);
    Site site = createAndDumpSite(configFile);

    dump("dir/resources/css/styles.css", "body { color: white; }");
    File template = dump("dir/template.html", "$<gen type=\"content\">");

    DummyFileAccess fileAccess =
      DummyFileAccess.init(template.lastModified())
        .addNow("/index.html")
        .addNow("/page1.html")
        .addNow("/page2.html")
        .addNow("/css/styles.css");

    checkUpload(site, fileAccess,
                "complete\n");

    boolean result = configFile.setLastModified(configFile.lastModified() + 5000);
    assertTrue(result);

    checkUpload(site, fileAccess,
                "update text: /index.html ==> $=Home\n" +
                "update text: /page1.html ==> $=Page1\n" +
                "update text: /page2.html ==> $=Page2\n" +
                "complete\n");
  }

  public void testReloadsPagesSubtreeInCaseOfTemplateChange() throws Exception {
    String descriptor =
      "<site url='http://www.siteweaver.org' pagesDir='content'>" +
      "  <page file='index.html' title='Home' template='template.html'>" +
      "    <page file='page1.html' title='Page1' template='template1.html'>" +
      "      <page file='page1/page1a.html' title='Page1a'/>" +
      "      <page file='page1/page1b.html' title='Page1b'/>" +
      "    </page>" +
      "    <page file='page2.html' title='Page2'/>" +
      "  </page>" +
      "  <copy baseDir='resources'>" +
      "    <file path='css/styles.css'/>" +
      "  </copy>" +
      "</site>";

    File configFile = dump("dir/siteweaver.xml", descriptor);
    Site site = createAndDumpSite(configFile);

    dump("dir/resources/css/styles.css", "body { color: white; }");
    dump("dir/template.html", "$<gen type=\"content\">");
    File template1 = dump("dir/template1.html", "%<gen type=\"content\">");

    DummyFileAccess fileAccess =
      DummyFileAccess.init(template1.lastModified())
        .addNow("/index.html")
        .addNow("/page1.html")
        .addNow("/page1/page1a.html")
        .addNow("/page1/page1b.html")
        .addNow("/page2.html")
        .addNow("/css/styles.css");

    checkUpload(site, fileAccess,
                "complete\n");

    boolean result = template1.setLastModified(template1.lastModified() + 5000);
    assertTrue(result);

    checkUpload(site, fileAccess,
                "update text: /page1.html ==> %=Page1\n" +
                "update text: /page1/page1a.html ==> %=Page1a\n" +
                "update text: /page1/page1b.html ==> %=Page1b\n" +
                "complete\n");
  }

  private void checkUpload(Site site, DummyFileAccess fileAccess, String actionsLog) throws IOException {
    SiteUploader uploader = new SiteUploader(site, fileAccess);
    uploader.run();
    fileAccess.checkActionsLog(actionsLog);
  }

  private Site createAndDumpSite(String configFilePath, String descriptor) throws Exception {
    File configFile = dump(configFilePath, descriptor);
    return createAndDumpSite(configFile);
  }

  private Site createAndDumpSite(File configFile) throws Exception {
    final Site site = SiteParser.parse(configFile);
    site.processPages(new PageFunctor() {
      public void process(Page page) throws Exception {
        dump(site.getInputFile(page), "=" + page.getTitle());
      }
    });
    return site;
  }

  private static class DummyFileAccess implements FileAccess {

    private List<FileHandle> handles = new ArrayList<FileHandle>();
    private StringBuilder builder = new StringBuilder();

    private final long now;
    private final long past;
    private final long future;

    public DummyFileAccess(long now) {
      this.past = now - 5000;
      this.future = now + 5000;
      this.now = now + 1;
    }

    public void addListener(FileAccessListener listener) {
    }

    public void removeListener(FileAccessListener listener) {
    }

    public void setApplyChanges(boolean applyChanges) {
    }

    public static DummyFileAccess init(long now) {
      return new DummyFileAccess(now);
    }

    public List<FileHandle> listAllFiles() throws IOException {
      return handles;
    }

    public DummyFileAccess addNow(String path) {
      return add(path, now);
    }

    public DummyFileAccess addPast(String path) {
      return add(path, past);
    }

    public DummyFileAccess addFuture(String path) {
      return add(path, future);
    }

    public DummyFileAccess add(String path, long timestamp) {
      handles.add(new FileHandle(path, timestamp));
      return this;
    }

    public void uploadText(String path, String content) {
      builder.append("update text: " + path + " ==> " + content + "\n");
    }

    public void uploadFile(String path, File file) {
      String localPath = file.getAbsolutePath().replace(tmpDir.getAbsolutePath(), "");
      builder.append("update file: " + path + " ==> upload from " + localPath + "\n");
    }

    public void delete(String path) {
      builder.append("delete file: " + path + "\n");
    }

    public void complete() {
      builder.append("complete\n");
    }

    public void dispose() {
      builder.append("dispose\n");
    }

    public void checkActionsLog(String expected) {
      assertEquals(expected, builder.toString());
      builder = new StringBuilder();
    }
  }
}
