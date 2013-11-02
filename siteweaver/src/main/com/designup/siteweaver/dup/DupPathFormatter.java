package com.designup.siteweaver.dup;

import com.designup.siteweaver.generation.AbstractFormatter;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

import java.io.IOException;

public class DupPathFormatter extends AbstractFormatter {

  private String separator;
  private String color;
  private boolean hideLast;

  public DupPathFormatter(HtmlTag tag) {
    if (tag.hasAttribute("imgsrc")) {
      StringBuffer strBuf = new StringBuffer(" <img");
      tag.addFormattedAttribute("imgsrc", "src", strBuf);
      tag.addFormattedAttribute("imgborder", "border", strBuf);
      tag.addFormattedAttribute("imgalign", "align", strBuf);
      tag.addFormattedAttribute("imgwidth", "width", strBuf);
      tag.addFormattedAttribute("imgheight", "height", strBuf);
      separator = strBuf.toString() + ">&nbsp;";
    }
    else {
      separator = " &gt ";
    }

    if (tag.hasAttribute("color")) {
      color = tag.getAttributeValue("color");
    }
    hideLast = "yes".equalsIgnoreCase(tag.getAttributeValue("hideLast"));
  }

  public void writeElement(Page page, Page target, HtmlWriter output)
    throws IOException {

    if (page != target) {
      output.write("<a href=\"" + page.getFileName() + "\">");
    }
    if (color != null) {
      output.write("<font color=\"" + color + "\">");
    }
    if ((page != target) || !hideLast) {
      output.write(page.getShortTitle());
    }
    if (color != null) {
      output.write("</font>");
    }
    if (page != target) {
      output.write("</a>");
    }
  }

  public void writeSeparator(HtmlWriter output) throws IOException {
    output.write(separator);
  }
}
