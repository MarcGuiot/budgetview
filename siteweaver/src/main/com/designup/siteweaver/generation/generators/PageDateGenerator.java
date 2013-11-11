package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Generator that writes the "last modified" date of the current page file.
 */
public class PageDateGenerator implements Generator {

  private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput)
    throws IOException {

    String inputFileName = site.getInputFilePath(page);
    File pageFile = new File(inputFileName);
    Date date = new Date(pageFile.lastModified());
    writer.write(dateFormat.format(date));
  }
}
