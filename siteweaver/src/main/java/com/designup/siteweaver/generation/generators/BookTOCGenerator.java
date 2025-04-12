package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class BookTOCGenerator implements Generator {

  public static final String ROOT_PATH = "root";
  public static final String MAX_DEPTH = "depth";

  private Formatter formatter;
  private final String rootPageFilePath;
  private final int maxDepth;

  public interface Formatter {
    void writeMenuStart(Page menuRootPage, HtmlWriter writer);

    void writeStart(HtmlWriter writer, int i);

    void writeElement(Page page, int depth, boolean active, HtmlWriter writer);

    void writeEnd(HtmlWriter writer, int i);

    void writeMenuEnd(HtmlWriter writer);
  }

  public BookTOCGenerator(HtmlTag tag, Formatter formatter) {
    this.formatter = formatter;
    this.rootPageFilePath = tag.getAttributeValue(ROOT_PATH);
    this.maxDepth = getMaxDepth(tag);
  }

  public void processPage(Site site, Page currentPage, HtmlWriter writer, HtmlOutput htmlOutput)
    throws IOException {

    Page root = getMenuRoot(currentPage);

    formatter.writeMenuStart(root, writer);
    writeSubPages(root, currentPage, 1, writer);
    formatter.writeMenuEnd(writer);
  }

  private void writeSubPages(Page pageInMenu, Page currentPage, int depth, HtmlWriter writer) {
    if (depth > maxDepth) {
      return;
    }
    formatter.writeStart(writer, depth);
    for (Page subPage : pageInMenu.getSubPages()) {
      formatter.writeElement(subPage, depth, currentPage.equals(subPage), writer);
      if (subPage.hasSubPages()) {
        writeSubPages(subPage, currentPage, depth + 1, writer);
      }
    }
    formatter.writeEnd(writer, depth);
  }

  private Page getMenuRoot(Page currentPage) {
    if (rootPageFilePath == null) {
      return currentPage;
    }
    return currentPage.getRootPage().getPageForFile(rootPageFilePath);
  }

  private int getMaxDepth(HtmlTag tag) {
    if (!tag.containsAttribute(MAX_DEPTH)) {
      throw new IllegalArgumentException("'depth' attribute must be set for this generator");
    }
    switch (tag.getIntValue(MAX_DEPTH)) {
      case 1:
        return 1;
      case 2:
        return 2;
      default:
        throw new IllegalArgumentException("BookTOC: attribute 'depth' must be 1 or 2");
    }
  }
}
