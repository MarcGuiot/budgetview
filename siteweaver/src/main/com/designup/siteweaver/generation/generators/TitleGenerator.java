package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class TitleGenerator implements Generator {

  private Formatter formatter;

  public interface Formatter {

    public void writeTitle(Page page, HtmlWriter writer);
  }

  public TitleGenerator(Formatter formatter) {
    this.formatter = formatter;
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput) throws IOException {
    if (!page.isTrue("title.hide", false, false)) {
      formatter.writeTitle(page, writer);
    }
  }
}
