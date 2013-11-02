package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Formatter;
import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;
import java.util.Iterator;

/**
 * Writes a "Navigation Bar"
 */
public class NavBarGenerator implements Generator {

  private Formatter formatter;

  public NavBarGenerator(Formatter associatedFormatter) {
    formatter = associatedFormatter;
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput)
    throws IOException {
    formatter.writeStart(writer);
    for (Iterator<Page> iter = page.getRootPage().getSubPages().iterator();
         iter.hasNext();) {
      Page subPage = iter.next();
      formatter.writeElement(subPage, page, writer);
      if (iter.hasNext()) {
        formatter.writeSeparator(writer);
      }
    }
    formatter.writeEnd(writer);
  }
}
