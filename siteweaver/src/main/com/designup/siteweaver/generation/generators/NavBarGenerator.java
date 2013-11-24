package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.utils.DefaultFormatter;
import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;
import java.util.Iterator;

public class NavBarGenerator implements Generator {

  private DefaultFormatter formatter;

  public NavBarGenerator(DefaultFormatter associatedFormatter) {
    formatter = associatedFormatter;
  }

  public void processPage(Site site, Page currentPage, HtmlWriter writer, HtmlOutput htmlOutput)
    throws IOException {
    formatter.writeStart(writer);
    formatter.writeElement(currentPage.getRootPage(), currentPage, writer);
    formatter.writeSeparator(writer);
    for (Iterator<Page> iter = currentPage.getRootPage().getSubPages().iterator(); iter.hasNext();) {
      Page otherPage = iter.next();
      if (otherPage.isTrue("navbar.hide", false, true)) {
        continue;
      }
      formatter.writeElement(otherPage, currentPage, writer);
      if (iter.hasNext()) {
        formatter.writeSeparator(writer);
      }
    }
    formatter.writeEnd(writer);
  }
}
