package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.IOException;

public class StaticBoxGenerator implements Generator {
  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput) throws IOException {
    String[] files = page.getBorderBoxesFiles();
    for (String file : files) {
      ContentGenerator.dumpFileContent(site.getInputDirectory() + "/" + file, writer);
    }
  }
}
