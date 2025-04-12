package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class InnerTemplateGenerator implements Generator {
  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput output) throws IOException {
    String innerTemplateFilePath = page.getValueForKey("innerTemplate", true);
    if (innerTemplateFilePath == null) {
      throw new IllegalArgumentException("No innerTemplate defined for page: " + page.getFilePath());
    }
    FileGenerator newGenerator = new FileGenerator(site.getInputDirectory() + "/" + innerTemplateFilePath);
    newGenerator.generatePage(page, site, writer, output);
  }
}
