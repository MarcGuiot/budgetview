package com.designup.siteweaver.custom.dup;

import com.designup.siteweaver.generation.generators.BookMenuGenerator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

public class DupBookMenuFormatter implements BookMenuGenerator.Formatter {

  public void writeMenuStart(Page menuRootPage, HtmlWriter writer) {
    writer.write("<div class=\"bookmenu\">\n");
    writer.write("<h2>");
    writer.writeLink(menuRootPage.getShortTitle(), menuRootPage.getUrl());
    writer.write("</h2>");
  }

  public void writeMenuEnd(HtmlWriter writer) {
    writer.write("</div>\n");
  }

  public void writeStart(HtmlWriter writer, int depth) {
    writer.write("<ul class=\"level" + depth + "\">");
  }

  public void writeElement(Page page, int depth, boolean active, HtmlWriter writer) {
    String shortTitle = page.getShortTitle();
    if (BookMenuGenerator.isMenuRoot(page)) {
      shortTitle = "Introduction";
    }
    if (active) {
      writer.write("<li class=\"active\">" + shortTitle + "</li>");
    }
    else {
      writer.write("<li>");
      writer.writeLink(shortTitle, page.getUrl());
      writer.write("</li>");
    }
  }

  public void writeEnd(HtmlWriter writer, int depth) {
    writer.write("</ul>");
  }
}
