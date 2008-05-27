package org.crossbowlabs.globs.utils.logging;

import java.util.List;

class HtmlTable {
  private HtmlLogger logger;

  public HtmlTable(HtmlLogger logger) {
    this.logger = logger;
    logger.write("<table>\n");
  }

  public void writeHeader(List<String> titles) {
    logger.write("<thead><tr>");
    for (String title : titles) {
      logger.write("<th>" + title + "</th>");
    }
    logger.write("</tr></thead>\n");
  }

  public void writeRow(List<String> cells) {
    logger.write("<tr>");
    for (String cell : cells) {
      logger.write("<td>" + cell + "</td>");
    }
    logger.write("</tr>\n");
  }

  public void end() {
    logger.write("</table>\n");
  }
}
