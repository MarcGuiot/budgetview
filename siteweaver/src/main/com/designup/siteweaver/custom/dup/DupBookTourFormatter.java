package com.designup.siteweaver.custom.dup;

import com.designup.siteweaver.generation.generators.BookTourGenerator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

public class DupBookTourFormatter implements BookTourGenerator.Formatter {
  public void writeStart(HtmlWriter writer) {
    writer.write("<div class=\"booktour\">");
  }

  public void writePath(Page nextPage, HtmlWriter writer) {
    writer.write(nextPage.getFileName());
  }

  public void writeLink(Page nextPage, HtmlWriter writer) {
    writer.writeLink(nextPage.getTitle(), nextPage.getUrl());
  }

  public void writeTitle(Page nextPage, HtmlWriter writer) {
    writer.write(nextPage.getTitle());
  }

  public void writeEnd(HtmlWriter writer) {
    writer.write("</div>");
  }
}
