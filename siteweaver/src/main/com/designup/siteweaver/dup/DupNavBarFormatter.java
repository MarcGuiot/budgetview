package com.designup.siteweaver.dup;

import com.designup.siteweaver.generation.AbstractFormatter;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

import java.io.IOException;

public class DupNavBarFormatter extends AbstractFormatter {

  public DupNavBarFormatter() {
  }

  public void writeElement(Page page, Page target, HtmlWriter output)
    throws IOException {

    output.write("[");
    if (target.isDescendantOf(page)) {
      output.write("<b>");
    }
    else {
      output.write("<a href=\"" + page.getFileName() + "\">");
    }
    output.write(page.getShortTitle());
    if (target.isDescendantOf(page)) {
      output.write("</b>");
    }
    else {
      output.write("</a>");
    }
    output.write("]");
  }

  public void writeSeparator(HtmlWriter output) throws IOException {
    output.write(" ");
  }
}
