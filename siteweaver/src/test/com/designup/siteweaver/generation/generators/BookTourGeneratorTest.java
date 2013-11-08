package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.generation.utils.GeneratorTestCase;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

public class BookTourGeneratorTest extends GeneratorTestCase {

  private Generator generator;

  protected Generator getGenerator() {
    return generator;
  }

  public void testNotShownOutsideOfBookMenus() throws Exception {
    initGenerator("path");
    checkOutput("", "");
    checkOutput("p1", "");
    checkOutput("p1_2_2", "");
  }

  public void testPath() throws Exception {
    getPage("").addKeyWithValue(BookMenuGenerator.MENU_ROOT, "true");
    initGenerator("path");
    checkOutput("", "[p1.html]");
    checkOutput("p1", "[p1_1.html]");
    checkOutput("p1_2_2", "[p1_2_3.html]");
    checkOutput("p1_2_3", "[p1_3.html]");
    checkOutput("p3_3_3", "[root.html]");
  }

  public void testLink() throws Exception {
    getPage("").addKeyWithValue(BookMenuGenerator.MENU_ROOT, "true");
    initGenerator("link");
    checkOutput("", "[/p1]");
    checkOutput("p1", "[/p1-1]");
    checkOutput("p1_2_2", "[/p1-2-3]");
    checkOutput("p1_2_3", "[/p1-3]");
    checkOutput("p3_3_3", "[/root]");
  }

  public void testTitle() throws Exception {
    getPage("").addKeyWithValue(BookMenuGenerator.MENU_ROOT, "true");
    initGenerator("title");
    checkOutput("", "[p1]");
    checkOutput("p1", "[p1_1]");
    checkOutput("p1_2_2", "[p1_2_3]");
    checkOutput("p1_2_3", "[p1_3]");
    checkOutput("p3_3_3", "[root]");
  }

  public void testTheUrlIsReturned() throws Exception {
    initGenerator("path");
    Page imagePage = new Page("image.html", "title", "shortTitle");
    imagePage.addKeyWithValue(BookMenuGenerator.MENU_ROOT, "true");
    checkOutput(imagePage, "[image.html]");
  }

  public void testDefaultOutputTypeIsLink() throws Exception {
    getPage("").addKeyWithValue(BookMenuGenerator.MENU_ROOT, "true");
    initGenerator(null);
    checkOutput("p1", "[/p1-1]");
  }

  private void initGenerator(String contentType) {
    HtmlTag tag = new HtmlTag();
    if (contentType != null) {
      tag.addAttribute("output", contentType);
    }
    generator = new BookTourGenerator(tag, new DummyBookTourFormatter());
  }

  private class DummyBookTourFormatter implements BookTourGenerator.Formatter {
    public void writeStart(HtmlWriter writer) {
      writer.write("[");
    }

    public void writePath(Page nextPage, HtmlWriter writer) {
      writer.write(nextPage.getFileName());
    }

    public void writeLink(Page nextPage, HtmlWriter writer) {
      writer.write(nextPage.getUrl());
    }

    public void writeTitle(Page nextPage, HtmlWriter writer) {
      writer.write(nextPage.getTitle());
    }

    public void writeEnd(HtmlWriter writer) {
      writer.write("]");
    }
  }
}
