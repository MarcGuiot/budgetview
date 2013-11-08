package com.designup.siteweaver.generation.utils;

import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

import java.io.IOException;

public class DummyFormatter implements DefaultFormatter {
  public void writeStart(HtmlWriter writer) throws IOException {
    writer.write("[");
  }

  public void writeEnd(HtmlWriter writer) throws IOException {
    writer.write("]");
  }

  public void writeSeparator(HtmlWriter writer) throws IOException {
    writer.write(",");
  }

  public void writeElement(Page page, Page target,
                           HtmlWriter writer) throws IOException {
    if (page == target) {
      writer.write("*");
    }
    if (target.isDescendantOf(page)) {
      writer.write("#");
    }
    writer.write("(" + page.getTitle() + ")");
  }

}
