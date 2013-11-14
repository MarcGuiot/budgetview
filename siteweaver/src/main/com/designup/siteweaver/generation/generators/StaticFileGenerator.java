package com.designup.siteweaver.generation.generators;

import com.designup.siteweaver.generation.Generator;
import com.designup.siteweaver.html.HtmlParser;
import com.designup.siteweaver.html.HtmlTag;
import com.designup.siteweaver.html.HtmlWriter;
import com.designup.siteweaver.html.output.HtmlOutput;
import com.designup.siteweaver.model.Page;
import com.designup.siteweaver.model.Site;
import com.designup.siteweaver.utils.FileUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class StaticFileGenerator implements Generator {
  public void processPage(Site site, Page page, HtmlWriter writer, HtmlOutput htmlOutput) throws IOException {
    String[] files = page.getBorderBoxesFiles();
    for (String file : files) {
      String inputFileName = site.getInputDirectory() + "/" + file;
      BufferedReader inputFile = new BufferedReader(new FileReader(inputFileName));

      HtmlParser parser = new HtmlParser(inputFile);
      HtmlTag tag = parser.findNextTag("body", null);
      if (tag != null) {
        parser.findNextTag("/body", writer);
      }
      else {
        FileUtils.copyFile(inputFileName, writer);
      }
    }
  }
}
