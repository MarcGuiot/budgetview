package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class StaticTextGenerator implements Generator {

  private String outputString;

  public StaticTextGenerator(String outputString) {
    this.outputString = outputString;
  }

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput)
    throws IOException {
    writer.write(outputString);
  }
}
