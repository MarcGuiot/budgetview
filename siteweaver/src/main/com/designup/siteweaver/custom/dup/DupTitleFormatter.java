package com.designup.siteweaver.custom.dup;

import com.designup.siteweaver.generation.generators.TitleGenerator;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.model.Page;

public class DupTitleFormatter implements TitleGenerator.Formatter {
  private boolean writeDiv;

  public DupTitleFormatter(HtmlTag tag) {
    writeDiv = tag.isTrue("writediv", false);
  }

  public void writeTitle(Page page, HtmlWriter writer) {
    if (writeDiv) {
      writer.write("<div class=\"row\">\n" +
                   "<h1>" + page.getTitle() + "</h1>\n" +
                   "</div>");
    }
    else {
      writer.write(page.getTitle());
    }
  }
}
