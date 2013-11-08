package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Formatter;
import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;
import java.util.Iterator;

public class NavBarGenerator implements Generator {

  private Formatter formatter;

  public NavBarGenerator(Formatter associatedFormatter) {
    formatter = associatedFormatter;
  }

  public void processPage(Site site, Page currentPage, HtmlWriter writer, HtmlOutput htmlOutput)
    throws IOException {
    formatter.writeStart(writer);
    formatter.writeElement(currentPage.getRootPage(), currentPage, writer);
    formatter.writeSeparator(writer);
    for (Iterator<Page> iter = currentPage.getRootPage().getSubPages().iterator(); iter.hasNext();) {
      Page otherPage = iter.next();
      formatter.writeElement(otherPage, currentPage, writer);
      if (iter.hasNext()) {
        formatter.writeSeparator(writer);
      }
    }
    formatter.writeEnd(writer);
  }
}
