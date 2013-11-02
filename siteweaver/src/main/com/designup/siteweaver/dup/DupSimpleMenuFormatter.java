package com.designup.siteweaver.dup;

import com.designup.siteweaver.generation.AbstractFormatter;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

import java.io.IOException;
import java.io.Writer;

public class DupSimpleMenuFormatter extends AbstractFormatter {

  private HtmlTag tag;
  private String indicator;

  public DupSimpleMenuFormatter(HtmlTag tag) {
    this.tag = tag;
    if (tag.hasAttribute("imgsrc")) {
      StringBuffer strBuf = new StringBuffer("<img");
      tag.addFormattedAttribute("imgsrc", "src", strBuf);
      tag.addFormattedAttribute("imgborder", "border", strBuf);
      tag.addFormattedAttribute("imgalign", "align", strBuf);
      tag.addFormattedAttribute("imgwidth", "width", strBuf);
      tag.addFormattedAttribute("imgheight", "height", strBuf);
      indicator = strBuf.toString() + ">";
    }
    else {
      indicator = "&gt;";
    }
  }

  public void writeStart(HtmlWriter output) throws IOException {
    output.startTag("table")
      .add("width", "100%")
      .add("border", "0")
      .add("cellpadding", "2")
      .add("cellspacing", "0")
      .add("bgcolor", tag.getAttributeValue("bgcolor"))
      .end();
  }

  public void writeEnd(HtmlWriter output) throws IOException {
    output.closeTag("table");
  }

  public void writeElement(Page page, Page target, HtmlWriter output)
    throws IOException {

    Page headerPage = findHeaderPage(target);
    if (headerPage == null) {
      return;
    }

    boolean isTarget = (page.equals(target));
    switch (computePageLevel(page, headerPage)) {
      case 1:
        writeLevel1Element(page, isTarget, output);
        break;
      case 2:
        writeLevel2Element(page, isTarget, output);
        break;
      case 3:
        writeLevel3Element(page, isTarget, output);
        break;
      default: // skip page
    }
  }

  private Page findHeaderPage(Page target) {
    Page page = target;
    while (page != null) {
      if (isHeaderPage(page)) {
        return page;
      }
      page = page.getParentPage();
    }
    return null;
  }

  private boolean isHeaderPage(Page page) {
    return page.hasKey("menu.header", false);
  }

  private int computePageLevel(Page currentPage, Page headerPage) {
    int level = 1;
    for (Page page = currentPage; page != null; page = page.getParentPage()) {
      if (page.equals(headerPage)) {
        return level;
      }
      level++;
    }
    return -1;
  }

  private void writeLevel1Element(Page page,
                                  boolean isTarget,
                                  HtmlWriter output) throws IOException {
    writeHeader(output, page);
    writeFirstIntroSection(output, isTarget, page);
  }

  private void writeHeader(HtmlWriter output, Page page) throws IOException {
    output.startTag("tr")
      .add("bgcolor", tag.getAttributeValue("titlebg"))
      .end();
    output.startTag("td")
      .add("align", "left")
      .add("colspan", "3")
      .end();
    output.startTag("div")
      .add("class", tag.getAttributeValue("titledivclass"))
      .end();
    output.writeTag("b");
    String titleClass =
      tag.hasAttribute("titleclass") ?
      " class=\"" + tag.getAttributeValue("titleclass", "titlelink") + "\"" : "";
    output.write("<a href=\"" + page.getFileName() + "\"" + titleClass + ">");
    output.write(page.getShortTitle());
    output.write("</a></b></div></td></tr>\n");
  }

  private void writeFirstIntroSection(Writer output, boolean isTarget, Page page) throws IOException {
    output.write("<tr>");
    writeIndicator(isTarget, output);
    output.write("<td>");
    writePageRef(output, "Introduction", page.getFileName(), isTarget);
    output.write("</td></tr>\n");
  }

  private void writeLevel2Element(Page page, boolean isTarget, Writer output)
    throws IOException {
    output.write("<tr>");
    writeIndicator(isTarget, output);
    output.write("<td>");
    writePageRef(output, page.getShortTitle(), page.getFileName(), isTarget);
    output.write("</td></tr>\n");
  }

  private void writeLevel3Element(Page page, boolean isTarget, Writer output)
    throws IOException {
    output.write("<tr>");
    writeIndicator(isTarget, output);
    output.write("<td><i><font style=\"margin-left:5\">");
    writePageRef(output, page.getShortTitle(), page.getFileName(), isTarget);
    output.write("</font></i></td></tr>");
  }

  private void writePageRef(Writer output, String title, String fileName, boolean isTarget) throws IOException {
    output.write("<font size=1>");
    if (isTarget) {
      output.write("<b>");
    }
    else {
      output.write("<a href=\"" + fileName + "\">");
    }
    output.write(title);
    if (isTarget) {
      output.write("</b>");
    }
    else {
      output.write("</a>");
    }
    output.write("</font>");
  }

  private void writeIndicator(boolean isTarget, Writer output)
    throws IOException {
    output.write("<td width=\"14\" align=\"right\">");
    if (isTarget) {
      output.write(indicator);
    }
    else {
      output.write("&nbsp;");
    }
    output.write("</td>");
    output.write("<td width=\"1\" style=\"background-repeat:repeat-y\" background=\"images/dashed_line.gif\">");
  }
}
