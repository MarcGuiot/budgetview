package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Formatter;
import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class MenuGenerator implements Generator {

  private Formatter formatter;

  public MenuGenerator(Formatter associatedFormatter) {
    formatter = associatedFormatter;
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput)
    throws IOException {
    formatter.writeStart(writer);
    browsePage(page.getRootPage(), page, 0, writer);
    formatter.writeEnd(writer);
  }

  private void browsePage(Page page, Page target, int level, HtmlWriter output)
    throws IOException {
    if (level != 0) {
      formatter.writeSeparator(output);
    }
    formatter.writeElement(page, target, output);
    if (target.isDescendantOf(page)) {
      for (Page subPage : page.getSubPages()) {
        browsePage(subPage, target, level + 1, output);
      }
    }
  }
}
