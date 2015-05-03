package com.designup.siteweaver.custom.dup;

import com.designup.siteweaver.generation.generators.HeaderGenerator;
import com.designup.siteweaver.generation.generators.TitleGenerator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

public class DupH1Formatter implements HeaderGenerator.Formatter {

  public void writeTitle(Page page, HtmlWriter writer) {
    String title = page.getTitle().replace(" ?", "&nbsp;?").replace(" :", "&nbsp;:");
    writer.write("<div class=\"row\">\n" +
                 "<h1>" + title + "</h1>\n" +
                 "</div>");
  }
}
