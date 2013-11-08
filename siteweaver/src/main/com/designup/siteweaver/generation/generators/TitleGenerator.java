package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class TitleGenerator implements Generator {

  private boolean show;

  public TitleGenerator(HtmlTag tag) {
    show = tag.isTrue("title.show", true);
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput) throws IOException {
    if (show) {
      writer.write(page.getTitle());
    }
  }
}
