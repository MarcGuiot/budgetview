package com.designup.siteweaver.custom.dup;

import com.designup.siteweaver.generation.generators.BookTOCGenerator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

public class DupBookTOCFormatter implements BookTOCGenerator.Formatter {

  public void writeMenuStart(Page menuRootPage, HtmlWriter writer) {
    writer.write("<div class=\"booktoc\">\n");
  }

  public void writeMenuEnd(HtmlWriter writer) {
    writer.write("</div>\n");
  }

  public void writeStart(HtmlWriter writer, int depth) {
    writer.write("<ul class=\"level" + depth + "\">");
  }

  public void writeElement(Page page, int depth, boolean active, HtmlWriter writer) {
    String classes = "level" + depth;
    if (active) {
      classes += " active";
    }
    writer.write("<li class=\"" + classes + "\">");
    writer.writeLink(page.getShortTitle(), page.getUrl());
    writer.write("</li>");
  }

  public void writeEnd(HtmlWriter writer, int depth) {
    writer.write("</ul>");
  }
}
