package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class BookMenuGenerator implements Generator {

  public static final String MENU_ROOT = "bookmenu.root";

  private Formatter formatter;

  public interface Formatter {
    void writeMenuStart(Page menuRootPage, HtmlWriter writer);

    void writeStart(HtmlWriter writer, int i);

    void writeElement(Page page, int depth, boolean active, HtmlWriter writer);

    void writeEnd(HtmlWriter writer, int i);

    void writeMenuEnd(HtmlWriter writer);
  }

  public BookMenuGenerator(Formatter formatter) {
    this.formatter = formatter;
  }

  public static boolean isMenuRoot(Page page) {
    return page.isTrue(MENU_ROOT, false, false);
  }

  public static boolean isInMenu(Page page) {
    for (Page parent = page; parent != null; parent = parent.getParentPage()) {
      if (isMenuRoot(parent)) {
        return true;
      }
    }
    return false;
  }

  public static Page getMenuRoot(Page currentPage) {
    for (Page page = currentPage; page != null; page = page.getParentPage()) {
      if (isMenuRoot(page)) {
        return page;
      }
    }
    return null;
  }

  public void processPage(Site site, Page currentPage, HtmlWriter writer, HtmlOutput htmlOutput)
    throws IOException {

    Page root = getMenuRoot(currentPage);
    if (root == null) {
      return;
    }

    formatter.writeMenuStart(root, writer);
    formatter.writeStart(writer, 0);
    formatter.writeElement(root, 0, currentPage.equals(root), writer);
    writeSubPages(root, currentPage, false, 0, writer);
    formatter.writeEnd(writer, 0);
    formatter.writeMenuEnd(writer);
  }

  private void writeSubPages(Page pageInMenu, Page currentPage, boolean showStartEnd, int depth, HtmlWriter writer) {
    if (showStartEnd) {
      formatter.writeStart(writer, depth);
    }
    for (Page page : pageInMenu.getSubPages()) {
      formatter.writeElement(page, depth, currentPage.equals(page), writer);
      if (page.hasSubPages() && (currentPage.equals(page) || currentPage.isDescendantOf(page))) {
        writeSubPages(page, currentPage, true, depth + 1, writer);
      }
    }
    if (showStartEnd) {
      formatter.writeEnd(writer, depth);
    }
  }
}
