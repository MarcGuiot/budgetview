package com.designup.siteweaver.custom.dup;

import com.designup.siteweaver.generation.utils.DefaultFormatter;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

import java.io.IOException;

public class DupNavBarFormatter implements DefaultFormatter {

  private String classAttr = "";

  public DupNavBarFormatter(HtmlTag tag) {
    if (tag.containsAttribute("class")) {
      classAttr = " class=\"" + tag.getAttributeValue("class") + "\"";
    }
  }

  public void writeStart(HtmlWriter writer) throws IOException {
    writer.write("<ul" + classAttr + ">");
  }

  public void writeEnd(HtmlWriter writer) throws IOException {
    writer.write("</ul>");
  }

  public void writeElement(Page subRootPage, Page currentPage, HtmlWriter writer)
    throws IOException {

    String activeClass = "";
    if ((currentPage.isRootPage() && subRootPage.isRootPage()) ||
        !currentPage.isRootPage() && !subRootPage.isRootPage() && currentPage.isDescendantOf(subRootPage)) {
      activeClass = " class=\"active\"";
    }
    writer.write("<li" + activeClass + "><a href=\"" + subRootPage.getUrl() + "\">");
    writer.write(subRootPage.getShortTitle());
    writer.write("</a></li>");
  }

  public void writeSeparator(HtmlWriter writer) throws IOException {
    writer.write(" ");
  }
}
