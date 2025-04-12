package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.HtmlTag;

import java.io.IOException;

public class UrlGenerator implements Generator {
  private boolean generateLink;

  public UrlGenerator(HtmlTag tag) {
    generateLink = tag.isBooleanAttributeSet("link");
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput) throws IOException {
    String url = htmlOutput.getBaseUrl(site) + "/" + page.getFilePath();
    if (generateLink) {
      writer.write("<a href=&quote;");
      writer.write(url);
      writer.write("&quote;>");
      writer.write(url);
      writer.write("</a>");
      writer.writeLink(url, url);
    }
    else {
      writer.write(url);
    }
  }
}
