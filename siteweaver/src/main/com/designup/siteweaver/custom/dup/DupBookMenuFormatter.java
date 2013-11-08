package com.designup.siteweaver.custom.dup;

import com.designup.siteweaver.generation.generators.BookMenuGenerator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

public class DupBookMenuFormatter implements BookMenuGenerator.Formatter {

  public void writeStart(HtmlWriter writer, int depth) {
    if (depth == 0) {
      writer.write("<div class=\"bookmenu\">");
    }
    writer.write("<ul>");
  }

  public void writeElement(Page page, int depth, boolean active, HtmlWriter writer) {
    if (active) {
      writer.write("<li class=\"active\">" + page.getShortTitle() + "</li>");
    }
    else {
      writer.write("<li>");
      writer.writeLink(page.getShortTitle(), page.getUrl());
      writer.write("</li>");
    }
  }

  public void writeEnd(HtmlWriter writer, int depth) {
    writer.write("</ul>");
    if (depth == 0) {
      writer.write("</div>");
    }
  }
}
