package com.designup.siteweaver.dup;

import com.designup.siteweaver.generation.AbstractFormatter;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

import java.io.IOException;
import java.io.StringWriter;

public class DupMenuFormatter extends AbstractFormatter {

  private boolean headerWritten;

  public void writeStart(HtmlWriter output) throws IOException {
    headerWritten = false;
  }

  private void writeHeaderIfNeeded(HtmlWriter output) {
    if (!headerWritten) {
    output.append(
      "<table bgcolor='#F7F7FF' width='200' align='center' border='0' cellspacing='0' cellpadding='0'>\n" +
      "  <tr>\n" +
      "    <td width='8' height='8' background='images/bricks/coin_haut_gauche_cadre.jpg' scope='col'></td>\n" +
      "    <td height='8' style='background-repeat:repeat-x' background='images/bricks/haut_cadre.jpg' scope='col'></td>\n" +
      "    <td width='8' height='8' background='images/bricks/coin_haut_droit_cadre.jpg' scope='col'></td>\n" +
      "  </tr>\n" +
      "  ");
      headerWritten = true;
    }
  }

  public void writeEnd(HtmlWriter output) throws IOException {
    if (!headerWritten) {
      return;
    }
    output.write(
      "       </table>\n" +
      "     </div>\n" +
      "    </td>\n" +
      "    <td width='8' style='background-repeat:repeat-y' background='images/bricks/droit_cadre.jpg'></td>\n" +
      "  </tr>\n" +
      "  <tr>\n" +
      "    <td width='8' height='8' background='images/bricks/coin_bas_gauche_cadre.jpg'></td>\n" +
      "    <td height='8' style='background-repeat:repeat-x' background='images/bricks/bas_cadre.jpg'></td>\n" +
      "    <td width='8' height='8' background='images/bricks/coin_bas_droit_cadre.jpg'></td>\n" +
      "  </tr>\n" +
      "</table>");
  }

  public void writeElement(Page page, Page target, HtmlWriter output)
    throws IOException {

    Page headerPage = findHeaderPage(target);
    if (headerPage == null) {
      return;
    }

    writeHeaderIfNeeded(output);

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

  private void writeLevel1Element(Page page,
                                  boolean isTarget,
                                  HtmlWriter output) throws IOException {
    writeHeader(output, page);
    writeInnerTableStart(output);
    writeFirstIntroSection(output, isTarget, page);
  }

  private void writeHeader(HtmlWriter output, Page page) {
    output.write(
      "<tr>" +
      "  <td width='8' height='24' style='background-repeat:no-repeat'" +
      "      background='images/bricks/gauche_haut_cadre.jpg'>" +
      "  </td>" +
      "  <td height='24' style='background-repeat:repeat-x;text-align:center'" +
      "      background='images/bricks/background_haut_cadre.jpg'>" +
      "    <a href='" + page.getFileName() + "'>" +
      "    <font color='#FFFFFF'>" +
      "    " + page.getShortTitle() +
      "    </font></a>" +
      "  </td>" +
      "  <td width='8' height='24' style='background-repeat:no-repeat'" +
      "    background='images/bricks/droit_haut_cadre.jpg'>" +
      "  </td>\n" +
      "</tr>" +
      "<tr>\n" +
      "  <td width='8' height='8' style='background-repeat:no-repeat'\n" +
      "      background='images/bricks/coin_bas_gauche_titre_cadre.jpg'>" +
      "  </td>\n" +
      "  <td height='8' style='background-repeat:repeat-x' background='images/bricks/bas_titre_cadre.jpg'>" +
      "  </td>\n" +
      "  <td width='8' height='8' style='background-repeat:no-repeat'\n" +
      "      background='images/bricks/coin_bas_droit_titre_cadre.jpg'></td>\n" +
      "</tr>"
    );
  }

  private void writeInnerTableStart(HtmlWriter output) {
    output.write(
      "<tr>\n" +
      "  <td width='8' style='background-repeat:repeat-y' background='images/bricks/gauche_cadre.jpg'></td>\n" +
      "  <td align='top' bgcolor='#F9F9F9'>\n" +
      "    <div class='menu'>\n" +
      "    <table width='100%' border='0' cellpadding='2' cellspacing='0'>");
  }

  private void writeFirstIntroSection(HtmlWriter output, boolean isTarget, Page page) {
    writeRow(output, isTarget, "Introduction", page.getOutputFileName(), 0);
  }

  private void writeRow(HtmlWriter output, boolean isTarget, String title, String fileName, int leftMargin) {
    String indicator =
      isTarget ? "<img src='images/fastforward.gif' border='0' align='left' width='14' height='7'>" : "&nbsp;";
    output.write(
      "<tr>\n" +
      "  <td width='14' align='right'>\n" +
      "    " + indicator +
      "  </td>\n" +
      "  <td width='1' style='background-repeat:repeat-y' background='images/dashed_line.gif'>\n" +
      "  <td>" + getPageRef(title, fileName, isTarget, leftMargin) + "</td>\n" +
      "</tr>");
  }

  private void writeLevel2Element(Page page, boolean isTarget, HtmlWriter output) {
    writeRow(output, isTarget, page.getShortTitle(), page.getOutputFileName(), 0);
  }

  private void writeLevel3Element(Page page, boolean isTarget, HtmlWriter output) {
    writeRow(output, isTarget, page.getShortTitle(), page.getOutputFileName(), 10);
  }

  private String getPageRef(String title, String fileName, boolean isTarget, int leftMargin) {
    StringWriter output = new StringWriter();
    output.write("<p style='margin-left:" + leftMargin + "px;margin-top:0px;margin-bottom:0px'><font size=1>");
    if (isTarget) {
      output.write("<b>");
    }
    else {
      output.write("<a href='" + fileName + "'>");
    }
    output.write(title);
    if (isTarget) {
      output.write("</b>");
    }
    else {
      output.write("</a>");
    }
    output.write("</font></p>");
    return output.toString();
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

}
