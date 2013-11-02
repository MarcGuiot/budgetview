package com.designup.siteweaver.generation.utils;

import com.designup.siteweaver.generation.Formatter;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

import java.io.IOException;

public class DummyFormatter implements Formatter {
  public void writeStart(HtmlWriter output) throws IOException {
    output.write("[");
  }

  public void writeEnd(HtmlWriter output) throws IOException {
    output.write("]");
  }

  public void writeSeparator(HtmlWriter output) throws IOException {
    output.write(",");
  }

  public void writeElement(Page page, Page target,
                           HtmlWriter output) throws IOException {
    if (page == target) {
      output.write("*");
    }
    if (target.isDescendantOf(page)) {
      output.write("#");
    }
    output.write("(" + page.getTitle() + ")");
  }

}
