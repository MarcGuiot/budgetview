package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class TitleGenerator implements Generator {

  private Formatter formatter;
  private String postfix;

  public interface Formatter {
    public void writeTitle(Page page, String title, HtmlWriter writer);
  }

  public TitleGenerator(Formatter formatter, String postfix) {
    this.formatter = formatter;
    this.postfix = postfix;
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput) throws IOException {
    String title = page.getValueForKey("title.override", false);
    if (title == null || title.isEmpty()) {
      title = page.getShortTitle();
    }
    if (title == null || title.isEmpty()) {
      title = page.getTitle();
    }
    formatter.writeTitle(page, title, writer);
  }
}
