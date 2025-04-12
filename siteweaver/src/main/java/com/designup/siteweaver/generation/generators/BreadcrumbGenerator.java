package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.utils.DefaultFormatter;
import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class BreadcrumbGenerator implements Generator {

  private DefaultFormatter formatter;

  public BreadcrumbGenerator(DefaultFormatter associatedFormatter) {
    formatter = associatedFormatter;
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput)
    throws IOException {
    formatter.writeStart(writer);
    if (page.hasParentPage()) {
      processParent(page.getParentPage(), page, writer);
      formatter.writeSeparator(writer);
    }
    formatter.writeElement(page, page, writer);
    formatter.writeEnd(writer);
  }

  private void processParent(Page page, Page target, HtmlWriter output)
    throws IOException {
    if (page.hasParentPage()) {
      processParent(page.getParentPage(), target, output);
      formatter.writeSeparator(output);
    }
    formatter.writeElement(page, target, output);
  }
}
