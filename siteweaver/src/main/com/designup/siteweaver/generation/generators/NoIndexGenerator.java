package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class NoIndexGenerator implements Generator {

  public NoIndexGenerator() {
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput) throws IOException {
    String noindex = page.getValueForKey("noindex", true);
    if ("true".equals(noindex)) {
      writer.write("<meta name=\"robots\" content=\"noindex\">");
    }
  }
}